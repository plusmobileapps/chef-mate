import AppIntents

/// "Hey Siri, add milk to my grocery list." Forwards the item to the iPhone over WatchConnectivity
/// (queued via `transferUserInfo`, so it's delivered even if the phone is briefly unreachable); the
/// phone adds it to the default list and syncs to Supabase.
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
        // Await WCSession activation: on a cold Siri launch the session isn't activated yet, and
        // firing the transfer before then drops the add.
        await WatchConnectivityManager.shared.sendAddItemAwaitingActivation(listId: nil, name: name)
        return .result(dialog: "Added \(name) to your grocery list.")
    }
}

struct ChefMateAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: AddGroceryItemIntent(),
            phrases: [
                "Add an item to my \(.applicationName) grocery list",
                "Add to my grocery list in \(.applicationName)",
            ],
            shortTitle: "Add Grocery Item",
            systemImageName: "cart.badge.plus"
        )
    }
}
