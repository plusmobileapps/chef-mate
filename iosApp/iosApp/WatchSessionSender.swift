import ComposeApp
import WatchConnectivity

/// iPhone side of the watch session handoff. Pushes the current Supabase access token to the watch
/// (on activation, on auth changes, and on request) via WatchConnectivity. Only the phone holds the
/// refresh token, so the shared token chain is never rotated from the watch.
final class WatchSessionSender: NSObject, WCSessionDelegate {
    private let relay: WatchSessionRelay
    private var cancelObserve: (() -> Void)?

    init(relay: WatchSessionRelay) {
        self.relay = relay
        super.init()
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    private func pushLatest() {
        relay.currentTokens { [weak self] accessToken, expiresAt in
            guard let self, WCSession.default.activationState == .activated else { return }
            try? WCSession.default.updateApplicationContext(Self.context(accessToken, expiresAt))
        }
    }

    private static func context(_ accessToken: String?, _ expiresAt: KotlinLong) -> [String: Any] {
        guard let accessToken else { return ["signedOut": true] }
        return ["accessToken": accessToken, "expiresAt": expiresAt.int64Value]
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        cancelObserve = relay.observeAuthChanges { [weak self] in self?.pushLatest() }
        pushLatest()
    }

    // Watch pulls a fresh token when it opens / its token expires.
    func session(
        _ session: WCSession,
        didReceiveMessage message: [String: Any],
        replyHandler: @escaping ([String: Any]) -> Void
    ) {
        relay.currentTokens { accessToken, expiresAt in
            replyHandler(Self.context(accessToken, expiresAt))
        }
    }

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }
}
