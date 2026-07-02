import Foundation
import WatchShared

/// Observable bridge between the shared Kotlin `WatchGroceryController` and SwiftUI. Reads are
/// pushed from Kotlin flows via callbacks (hopped onto the main actor); writes are `async` and
/// forwarded to the controller, which is offline-first + Supabase-backed.
@MainActor
final class GroceryStore: ObservableObject {
    @Published private(set) var lists: [WatchGroceryList] = []
    @Published private(set) var itemsByListId: [Int64: [WatchGroceryItem]] = [:]
    @Published private(set) var isSignedIn: Bool = false

    private let controller: WatchGroceryController
    private var listsHandle: WatchCancellable?
    private var signedInHandle: WatchCancellable?
    private var itemHandles: [Int64: WatchCancellable] = [:]

    init(controller: WatchGroceryController = SharedController.shared) {
        self.controller = controller

        signedInHandle = controller.observeSignedIn { [weak self] signedIn in
            let value = signedIn.boolValue
            Task { @MainActor in self?.isSignedIn = value }
        }
        listsHandle = controller.observeLists { [weak self] lists in
            Task { @MainActor in self?.lists = lists }
        }
    }

    func startObservingItems(listId: Int64) {
        guard itemHandles[listId] == nil else { return }
        itemHandles[listId] = controller.observeItems(listId: listId) { [weak self] items in
            Task { @MainActor in self?.itemsByListId[listId] = items }
        }
    }

    func items(for listId: Int64) -> [WatchGroceryItem] {
        itemsByListId[listId] ?? []
    }

    func addItem(listId: Int64, name: String) async {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        try? await controller.addItem(listId: listId, name: trimmed)
        try? await controller.syncNow()
    }

    func setChecked(itemId: Int64, isChecked: Bool) async {
        try? await controller.setChecked(itemId: itemId, isChecked: isChecked)
    }

    func syncNow() async {
        try? await controller.syncNow()
    }

    func importSession(refreshToken: String) async {
        try? await controller.importSession(refreshToken: refreshToken)
    }
}
