import Foundation
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

    /// Set by the store; invoked on the main queue whenever a fresh snapshot arrives.
    var onSnapshot: ((WatchSnapshot) -> Void)?

    private override init() {
        super.init()
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    /// Ask the phone for the latest snapshot (call when the watch becomes active).
    func requestSnapshot() {
        let session = WCSession.default
        guard session.activationState == .activated, session.isReachable else { return }
        session.sendMessage(["type": "requestSnapshot"], replyHandler: { [weak self] reply in
            self?.decode(reply)
        }, errorHandler: nil)
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
