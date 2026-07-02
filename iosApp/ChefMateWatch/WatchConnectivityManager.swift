import Foundation
import WatchConnectivity

/// Receives the Supabase session handed off from the iPhone. The phone pushes the current
/// refresh token via `updateApplicationContext(["supabaseRefreshToken": ...])` whenever its
/// session changes; the watch imports it, which signs the watch in and kicks off sync.
///
/// (The iPhone-side sender is a follow-up — it needs a small refresh-token accessor on the shared
/// `AuthenticationRepository`.)
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

    private func handle(context: [String: Any]) {
        guard let token = context["supabaseRefreshToken"] as? String, !token.isEmpty else { return }
        Task { await store.importSession(refreshToken: token) }
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        // Adopt any context already delivered before activation completed.
        handle(context: session.receivedApplicationContext)
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        handle(context: applicationContext)
    }
}
