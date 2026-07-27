import AppIntents
import ComposeApp

/// "Hey Siri, add milk to my Chef Mate grocery list." Runs on the phone (no watch required): it adds
/// the item to the default list through the shared `WatchDataBridge`, which persists it and syncs to
/// Supabase — the same path the watch's add action flows through.
///
/// Runs without opening the app (`openAppWhenRun = false`). When Siri triggers this while the app is
/// not running, the system background-launches it, which builds the DI graph in `AppDelegate` before
/// `perform()` runs. `awaitBridge()` still guards the cold-launch race so we never touch the
/// `lateinit` bridge before it exists.
struct AddGroceryItemIntent: AppIntent {
    static var title: LocalizedStringResource = "Add Grocery Item"
    static var description = IntentDescription("Adds an item to your Chef Mate grocery list.")
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Item", requestValueDialog: "What should I add?")
    var item: String

    static var parameterSummary: some ParameterSummary {
        Summary("Add \(\.$item) to my grocery list")
    }

    func perform() async throws -> some IntentResult & ProvidesDialog {
        let name = item.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !name.isEmpty else {
            return .result(dialog: "I didn't catch what to add.")
        }

        guard let bridge = await Self.awaitBridge() else {
            return .result(dialog: "Open Chef Mate once, then try adding that again.")
        }

        await withCheckedContinuation { continuation in
            bridge.ensureDefaultList { listId in
                // Resume only once the add has actually been persisted. `addItem` applies the write
                // on a coroutine, so resuming as soon as it returned let `perform()` finish — and
                // the system tear down the background-launched process — before the item was saved.
                bridge.addItem(listId: listId.int64Value, name: name) { continuation.resume() }
            }
        }

        return .result(dialog: "Added \(name) to your grocery list.")
    }

    /// Waits briefly for the DI graph to finish building on a cold background launch. Returns nil if
    /// the bridge never appears (app never fully launched), so the caller can prompt the user.
    private static func awaitBridge() async -> WatchDataBridge? {
        for _ in 0 ..< 50 {  // up to ~5s
            if let bridge = RootBlocProvider.shared.watchDataBridgeOrNull {
                return bridge
            }
            try? await Task.sleep(nanoseconds: 100_000_000)  // 0.1s
        }
        return RootBlocProvider.shared.watchDataBridgeOrNull
    }
}

/// The iPhone app's single App Shortcuts entry. Each phrase must include the app name (Siri routes
/// generic "add to my grocery list" wording to Reminders), so all phrases reference `Chef Mate` via
/// `\(.applicationName)`.
struct ChefMateAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: AddGroceryItemIntent(),
            phrases: [
                "Add an item to my \(.applicationName) grocery list",
                "Add to my grocery list in \(.applicationName)",
                "Add to my \(.applicationName) grocery list",
            ],
            shortTitle: "Add Grocery Item",
            systemImageName: "cart.badge.plus"
        )
    }
}
