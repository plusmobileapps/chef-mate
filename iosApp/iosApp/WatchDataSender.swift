import ComposeApp
import WatchConnectivity

/// iPhone side of the watch companion. The phone is the source of truth: it pushes grocery
/// snapshots to the watch and applies the watch's toggle/add actions through the shared
/// `WatchDataBridge` (which syncs to Supabase). The watch itself holds no Kotlin/Supabase.
final class WatchDataSender: NSObject, WCSessionDelegate {
    private let bridge: WatchDataBridge
    private var cancelObserve: (() -> Void)?

    init(bridge: WatchDataBridge) {
        self.bridge = bridge
        super.init()
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    private func push(_ json: String) {
        guard WCSession.default.activationState == .activated else { return }
        try? WCSession.default.updateApplicationContext(["snapshot": json])
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        // Push a fresh snapshot to the watch on every grocery data change.
        cancelObserve = bridge.observeSnapshot { [weak self] json in self?.push(json) }
    }

    // Watch pulls the latest snapshot when it opens.
    func session(
        _ session: WCSession,
        didReceiveMessage message: [String: Any],
        replyHandler: @escaping ([String: Any]) -> Void
    ) {
        if message["type"] as? String == "requestSnapshot" {
            bridge.currentSnapshot { json in replyHandler(["snapshot": json]) }
        } else {
            replyHandler([:])
        }
    }

    // Watch toggle/add actions (queued, reliable).
    func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any]) {
        switch userInfo["type"] as? String {
        case "setChecked":
            if let itemId = (userInfo["itemId"] as? NSNumber)?.int64Value,
               let isChecked = userInfo["isChecked"] as? Bool {
                bridge.setChecked(itemId: itemId, isChecked: isChecked)
            }
        case "addItem":
            guard let name = userInfo["name"] as? String, !name.isEmpty else { return }
            if let listId = (userInfo["listId"] as? NSNumber)?.int64Value {
                bridge.addItem(listId: listId, name: name)
            } else {
                bridge.ensureDefaultList { listId in
                    self.bridge.addItem(listId: listId.int64Value, name: name)
                }
            }
        default:
            break
        }
    }

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }
}
