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

    /// Applies one watch action, invoking `done` once it has been durably handled.
    ///
    /// Shared by both delivery routes: the watch sends mutations with `sendMessage` when the phone
    /// is reachable and falls back to a queued `transferUserInfo`, so the same payload can arrive
    /// through either delegate callback and must behave identically.
    private func apply(_ payload: [String: Any], _ done: @escaping () -> Void) {
        let type = payload["type"] as? String
        switch type {
        case "requestSnapshot":
            bridge.currentSnapshot { [weak self] json in
                self?.push(json)
                done()
            }
        case "setChecked":
            guard let itemId = (payload["itemId"] as? NSNumber)?.int64Value,
                  let isChecked = (payload["isChecked"] as? NSNumber)?.boolValue else {
                log.error("setChecked payload malformed: \(payload.keys.sorted())")
                return done()
            }
            log.info("applying setChecked (itemId=\(itemId), isChecked=\(isChecked))")
            bridge.setChecked(itemId: itemId, isChecked: isChecked, onComplete: done)
        case "addItem":
            guard let name = payload["name"] as? String, !name.isEmpty else {
                log.error("addItem payload malformed: \(payload.keys.sorted())")
                return done()
            }
            log.info("applying addItem")
            if let listId = (payload["listId"] as? NSNumber)?.int64Value {
                bridge.addItem(listId: listId, name: name, onComplete: done)
            } else {
                bridge.ensureDefaultList { [weak self] listId in
                    guard let self else { return done() }
                    self.bridge.addItem(listId: listId.int64Value, name: name, onComplete: done)
                }
            }
        default:
            log.error("ignoring unknown watch payload type: \(type ?? "nil")")
            done()
        }
    }

    // Immediate path: the watch uses `sendMessage` for snapshot pulls and, when the phone is
    // reachable, for mutations too. Replying is what releases the watch's send.
    func session(
        _ session: WCSession,
        didReceiveMessage message: [String: Any],
        replyHandler: @escaping ([String: Any]) -> Void
    ) {
        if message["type"] as? String == "requestSnapshot" {
            bridge.currentSnapshot { json in replyHandler(["snapshot": json]) }
        } else {
            // Reply straight away — the watch only needs the handoff acknowledged, and holding the
            // reply until the write finished would risk tripping WatchConnectivity's reply timeout.
            replyHandler([:])
            keepingAlive("watch-message") { [weak self] done in
                guard let self else { return done() }
                self.apply(message, done)
            }
        }
    }

    // Queued path: whatever the watch couldn't hand over immediately (phone out of range, or the
    // watch acted while the phone app wasn't reachable).
    //
    // Runs inside `keepingAlive` because this is usually a background wake, where returning from
    // here is what gets us suspended — see that method for why an unguarded bridge call silently
    // loses the watch's edit.
    func session(_ session: WCSession, didReceiveUserInfo userInfo: [String: Any]) {
        log.info("didReceiveUserInfo: \(userInfo["type"] as? String ?? "?")")
        keepingAlive("watch-userinfo") { [weak self] done in
            guard let self else { return done() }
            self.apply(userInfo, done)
        }
    }

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }
}
