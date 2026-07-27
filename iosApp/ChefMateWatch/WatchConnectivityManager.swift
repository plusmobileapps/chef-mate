import Foundation
import os
import WatchConnectivity

/// The watch's only link to its data: WatchConnectivity to the iPhone. The phone pushes grocery
/// snapshots (which we decode and hand to the UI) and we forward the user's toggle/add actions to
/// the phone (which applies them and syncs to Supabase). Mutations go via `transferUserInfo` so
/// they're queued and delivered even if the phone is momentarily unreachable.
///
/// A singleton so both the SwiftUI app and the Siri `AddGroceryItemIntent` share one activated
/// session.
final class WatchConnectivityManager: NSObject, WCSessionDelegate {
    static let shared = WatchConnectivityManager()

    private let log = Logger(subsystem: "com.plusmobileapps.chefmate.watch", category: "connectivity")

    /// Set by the store; invoked on the main queue whenever a fresh snapshot arrives.
    var onSnapshot: ((WatchSnapshot) -> Void)?

    private override init() {
        super.init()
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    /// Ask the phone for the latest snapshot (call when the watch becomes active).
    ///
    /// When the phone is reachable we use `sendMessage` for an immediate reply. When it isn't (the
    /// common case — the phone app is backgrounded or killed), we fall back to a queued
    /// `transferUserInfo` request so the phone still wakes, pushes a fresh snapshot, and the watch
    /// can recover on its own instead of getting stuck on the "Open Chef Mate on iPhone" prompt.
    func requestSnapshot() {
        let session = WCSession.default
        guard session.activationState == .activated else {
            log.info("requestSnapshot skipped: session not activated")
            return
        }
        if session.isReachable {
            session.sendMessage(["type": "requestSnapshot"], replyHandler: { [weak self] reply in
                self?.decode(reply)
            }, errorHandler: { [weak self] error in
                self?.log.error("sendMessage failed, falling back to userInfo: \(error.localizedDescription)")
                self?.requestSnapshotViaUserInfo()
            })
        } else {
            requestSnapshotViaUserInfo()
        }
    }

    private func requestSnapshotViaUserInfo() {
        WCSession.default.transferUserInfo(["type": "requestSnapshot"])
    }

    /// Sends a mutation to the phone, preferring immediate delivery over a queued transfer.
    ///
    /// `transferUserInfo` on its own is not enough. It is an *opportunistic* transfer — the system
    /// delivers it whenever it feels like it, which can be many minutes — so a checked item would
    /// routinely still be sitting in the queue when the phone next pushed a snapshot built without
    /// it, silently undoing the change. `sendMessage` delivers now and background-launches the phone
    /// app if it isn't running; this is the same pattern `requestSnapshot` already uses, which is
    /// why that path worked while these two didn't. The queued transfer stays as the out-of-range
    /// fallback so nothing is lost when the phone genuinely isn't there.
    private func send(_ info: [String: Any]) {
        let session = WCSession.default
        let type = info["type"] as? String ?? "?"
        guard session.activationState == .activated else {
            log.error("send(\(type)) skipped: session not activated")
            return
        }
        guard session.isReachable else {
            log.info("send(\(type)): phone unreachable, queueing transferUserInfo")
            session.transferUserInfo(info)
            return
        }
        session.sendMessage(info, replyHandler: { [weak self] _ in
            self?.log.info("send(\(type)): delivered via sendMessage")
        }, errorHandler: { [weak self] error in
            self?.log.error("send(\(type)) sendMessage failed, queueing: \(error.localizedDescription)")
            session.transferUserInfo(info)
        })
    }

    func sendSetChecked(itemId: Int64, isChecked: Bool) {
        send(["type": "setChecked", "itemId": itemId, "isChecked": isChecked])
    }

    /// `listId == nil` tells the phone to use the default list (used by the Siri intent).
    ///
    /// [send] drops the call if the session isn't activated yet — sending before then is a
    /// programmer error that throws. On a cold Siri launch use `sendAddItemAwaitingActivation`,
    /// which waits for activation first.
    func sendAddItem(listId: Int64?, name: String) {
        var info: [String: Any] = ["type": "addItem", "name": name]
        if let listId { info["listId"] = listId }
        send(info)
    }

    /// Adds an item, waiting up to ~5s for the session to activate first.
    ///
    /// The Siri `AddGroceryItemIntent` can cold-launch the watch app in the background. In that case
    /// the singleton's `activate()` (in `init`) hasn't finished — activation completes asynchronously
    /// in `activationDidCompleteWith` — so a plain `sendAddItem` would hit the not-activated guard and
    /// silently drop the add. Awaiting activation also keeps the intent's `perform()` alive until the
    /// transfer is handed to the system daemon, which is what makes queued delivery reliable.
    func sendAddItemAwaitingActivation(listId: Int64?, name: String) async {
        for _ in 0 ..< 50 {  // up to ~5s
            if WCSession.default.activationState == .activated { break }
            try? await Task.sleep(nanoseconds: 100_000_000)  // 0.1s
        }
        sendAddItem(listId: listId, name: name)
    }

    private func decode(_ payload: [String: Any]) {
        guard
            let jsonString = payload["snapshot"] as? String,
            let data = jsonString.data(using: .utf8),
            let snapshot = try? JSONDecoder().decode(WatchSnapshot.self, from: data)
        else { return }
        DispatchQueue.main.async { self.onSnapshot?(snapshot) }
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        decode(session.receivedApplicationContext)
        requestSnapshot()
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        decode(applicationContext)
    }

    /// Reports the fate of a queued `transferUserInfo`. Without this a fallback transfer that the
    /// system never delivered looked identical to one the phone silently ignored, which is what made
    /// the watch→phone direction so hard to pin down.
    func session(
        _ session: WCSession,
        didFinish userInfoTransfer: WCSessionUserInfoTransfer,
        error: Error?
    ) {
        let type = userInfoTransfer.userInfo["type"] as? String ?? "?"
        if let error {
            log.error("queued transfer(\(type)) failed: \(error.localizedDescription)")
        } else {
            log.info("queued transfer(\(type)) delivered")
        }
    }
}
