import ComposeApp
import os
import WatchConnectivity

/// iPhone side of the watch companion. The phone is the source of truth: it pushes grocery
/// snapshots to the watch and applies the watch's toggle/add actions through the shared
/// `WatchDataBridge` (which syncs to Supabase). The watch itself holds no Kotlin/Supabase.
final class WatchDataSender: NSObject, WCSessionDelegate {
    private let bridge: WatchDataBridge
    private var cancelObserve: (() -> Void)?
    private let log = Logger(subsystem: "com.plusmobileapps.chefmate", category: "watch")

    init(bridge: WatchDataBridge) {
        self.bridge = bridge
        super.init()
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    private func push(_ json: String) {
        let session = WCSession.default
        guard session.activationState == .activated else {
            log.info("push skipped: session not activated")
            return
        }
        // Surface failures instead of swallowing them: a thrown error here (e.g. the phone thinks
        // the watch app isn't installed) is exactly why the watch can silently never sync.
        do {
            try session.updateApplicationContext(["snapshot": json])
        } catch {
            log.error(
                "updateApplicationContext failed (paired=\(session.isPaired), watchAppInstalled=\(session.isWatchAppInstalled)): \(error.localizedDescription)"
            )
        }
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        if let error {
            log.error("activation failed: \(error.localizedDescription)")
        }
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

    // Watch toggle/add actions plus its queued snapshot request (all reliable, delivered even when
    // the phone was unreachable when the watch acted).
    func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any]) {
        switch userInfo["type"] as? String {
        case "requestSnapshot":
            // Watch asked while we were unreachable; push the current snapshot so it can load.
            bridge.currentSnapshot { [weak self] json in self?.push(json) }
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
