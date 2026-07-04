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

    func sendSetChecked(itemId: Int64, isChecked: Bool) {
        WCSession.default.transferUserInfo(["type": "setChecked", "itemId": itemId, "isChecked": isChecked])
    }

    /// `listId == nil` tells the phone to use the default list (used by the Siri intent).
    func sendAddItem(listId: Int64?, name: String) {
        var info: [String: Any] = ["type": "addItem", "name": name]
        if let listId { info["listId"] = listId }
        WCSession.default.transferUserInfo(info)
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
}
