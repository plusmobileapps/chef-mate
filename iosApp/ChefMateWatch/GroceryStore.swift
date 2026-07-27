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

    /// Checks made here that the phone hasn't confirmed yet, keyed by item id.
    ///
    /// Our mutation goes out on `transferUserInfo`, so the phone can already be mid-push with
    /// pre-toggle data when it lands — that snapshot legitimately still shows the old value and
    /// would visibly undo the tap. Re-applying pending checks on top of every incoming snapshot
    /// holds the user's change until the phone agrees with it. Entries also expire, so a mutation
    /// the phone genuinely dropped surfaces instead of being masked here forever.
    private var pendingChecks: [Int64: (isChecked: Bool, sentAt: Date)] = [:]
    private let pendingCheckTimeout: TimeInterval = 60

    init() {
        connectivity.onSnapshot = { [weak self] snapshot in
            self?.apply(snapshot)
        }
    }

    private func apply(_ incoming: WatchSnapshot) {
        let now = Date()
        pendingChecks = pendingChecks.filter { id, pending in
            guard now.timeIntervalSince(pending.sentAt) < pendingCheckTimeout else { return false }
            guard let item = incoming.items.first(where: { $0.id == id }) else { return false }
            return item.isChecked != pending.isChecked  // still unconfirmed — keep overriding
        }
        snapshot = incoming.applyingChecked(pendingChecks.mapValues(\.isChecked))
        hasLoaded = true
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
        pendingChecks[itemId] = (isChecked: isChecked, sentAt: Date())
        snapshot = snapshot.applyingChecked([itemId: isChecked])
        connectivity.sendSetChecked(itemId: itemId, isChecked: isChecked)
    }

    func addItem(listId: Int64, name: String) {
        let trimmed = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return }
        connectivity.sendAddItem(listId: listId, name: trimmed)
    }
}
