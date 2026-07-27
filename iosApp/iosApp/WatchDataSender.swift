import ComposeApp
import UIKit
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
        // Push a fresh snapshot to the watch on every grocery data change. Drop any previous
        // observer first: `sessionDidDeactivate` re-activates the session, so this runs more than
        // once per launch and would otherwise stack up a collector per activation.
        cancelObserve?()
        cancelObserve = bridge.observeSnapshot { [weak self] json in self?.push(json) }
    }

    /// Keeps the app awake until an async watch mutation has actually been applied.
    ///
    /// iOS wakes this app in the background purely to deliver a queued `transferUserInfo` payload
    /// and suspends it again as soon as the delegate callback returns. Every bridge call applies its
    /// mutation on a Kotlin coroutine and returns immediately, so without an assertion the write is
    /// killed mid-flight — the watch's edit never reaches the database, and the phone's next
    /// snapshot push overwrites it on the watch. `work` must invoke the completion it is handed once
    /// the mutation is durable.
    private func keepingAlive(_ name: String, _ work: @escaping (@escaping () -> Void) -> Void) {
        // `beginBackgroundTask` is a UIApplication API and WCSession delivers on a background queue,
        // so hop to main — and keep the token main-confined so the expiration handler and the
        // completion can't race each other into ending the same task twice.
        DispatchQueue.main.async {
            var token = UIBackgroundTaskIdentifier.invalid
            let finish = {
                guard token != .invalid else { return }
                UIApplication.shared.endBackgroundTask(token)
                token = .invalid
            }
            token = UIApplication.shared.beginBackgroundTask(withName: name) { [weak self] in
                self?.log.error("background task '\(name)' expired before the watch action finished")
                finish()
            }
            work { DispatchQueue.main.async(execute: finish) }
        }
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
    //
    // Every branch runs inside `keepingAlive` because this is usually called on a background wake,
    // where returning from here is what gets us suspended — see that method for why an unguarded
    // bridge call silently loses the watch's edit.
    func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any]) {
        switch userInfo["type"] as? String {
        case "requestSnapshot":
            // Watch asked while we were unreachable; push the current snapshot so it can load.
            keepingAlive("watch-snapshot") { [weak self] done in
                guard let self else { return done() }
                self.bridge.currentSnapshot { json in
                    self.push(json)
                    done()
                }
            }
        case "setChecked":
            guard let itemId = (userInfo["itemId"] as? NSNumber)?.int64Value,
                  let isChecked = userInfo["isChecked"] as? Bool else { return }
            keepingAlive("watch-set-checked") { [weak self] done in
                guard let self else { return done() }
                self.bridge.setChecked(itemId: itemId, isChecked: isChecked, onComplete: done)
            }
        case "addItem":
            guard let name = userInfo["name"] as? String, !name.isEmpty else { return }
            keepingAlive("watch-add-item") { [weak self] done in
                guard let self else { return done() }
                if let listId = (userInfo["listId"] as? NSNumber)?.int64Value {
                    self.bridge.addItem(listId: listId, name: name, onComplete: done)
                } else {
                    self.bridge.ensureDefaultList { listId in
                        self.bridge.addItem(listId: listId.int64Value, name: name, onComplete: done)
                    }
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
