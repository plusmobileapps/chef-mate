import AppIntents

/// "Hey Siri, add milk to my grocery list." Runs against the same shared controller as the app,
/// so the item lands in the local DB and syncs to Supabase (and thus to the phone).
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
        let controller = SharedController.shared
        // suspend functions returning a Kotlin primitive bridge to a boxed type (KotlinLong).
        let listId = (try await controller.ensureDefaultList()).int64Value
        try await controller.addItem(listId: listId, name: item)
        try await controller.syncNow()
        return .result(dialog: "Added \(item) to your grocery list.")
    }
}

struct ChefMateAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        // Free-form String parameters can't be interpolated into Siri phrases (only AppEntity /
        // AppEnum can), so the phrases are static and Siri prompts for the item via the
        // parameter's `requestValueDialog`.
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
