import Foundation
import WatchConnectivity

/// Receives the Supabase access token handed off from the iPhone and imports it (which signs the
/// watch in and kicks off sync). The phone pushes the token via `updateApplicationContext` on auth
/// changes; the watch also actively pulls a fresh token via `sendMessage` when it opens, so it
/// never depends on a stale context. The watch never holds the refresh token — only the phone
/// refreshes, avoiding token-rotation conflicts.
final class WatchConnectivityManager: NSObject, WCSessionDelegate {
    private let store: GroceryStore

    init(store: GroceryStore) {
        self.store = store
        super.init()
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
    }

    /// Pull the freshest access token from the phone. Call when the watch app becomes active.
    func requestSession() {
        let session = WCSession.default
        guard session.activationState == .activated else { return }
        session.sendMessage(["request": "session"], replyHandler: { [weak self] reply in
            self?.apply(reply)
        }, errorHandler: nil)
    }

    private func apply(_ payload: [String: Any]) {
        guard
            let token = payload["accessToken"] as? String,
            let expiresAt = (payload["expiresAt"] as? NSNumber)?.int64Value
        else { return }
        Task { await store.importSession(accessToken: token, expiresAtEpochSeconds: expiresAt) }
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        apply(session.receivedApplicationContext)
        requestSession()
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        apply(applicationContext)
    }
}
