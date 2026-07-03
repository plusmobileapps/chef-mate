import Foundation
import SwiftUI

/// Holds the grocery snapshot the phone sends, and forwards the user's actions back to the phone.
/// Optimistically applies toggles locally so the UI feels instant; the phone's next snapshot push
/// reconciles.
@MainActor
final class GroceryStore: ObservableObject {
    @Published private(set) var snapshot: WatchSnapshot = .empty
    @Published private(set) var hasLoaded = false

    private let connectivity = WatchConnectivityManager.shared

    init() {
        connectivity.onSnapshot = { [weak self] snapshot in
            self?.snapshot = snapshot
            self?.hasLoaded = true
        }
    }

    var lists: [WatchGroceryList] { snapshot.lists }

    func items(for listId: Int64) -> [WatchGroceryItem] {
        snapshot.items.filter { $0.listId == listId }
    }

    /// Pull the latest snapshot from the phone (call when the app becomes active).
    func refresh() {
        connectivity.requestSnapshot()
    }

    func setChecked(itemId: Int64, isChecked: Bool) {
        let updated = snapshot.items.map { $0.id == itemId ? $0.toggled() : $0 }
        snapshot = WatchSnapshot(lists: snapshot.lists, items: updated)
        connectivity.sendSetChecked(itemId: itemId, isChecked: isChecked)
    }

    func addItem(listId: Int64, name: String) {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        connectivity.sendAddItem(listId: listId, name: trimmed)
    }
}
