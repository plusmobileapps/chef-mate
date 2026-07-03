import Foundation

/// Native mirror of the JSON snapshot the phone sends over WatchConnectivity. The watch holds no
/// Kotlin/Supabase — the phone (source of truth) serializes its grocery data into this shape.
struct WatchSnapshot: Codable {
    let lists: [WatchGroceryList]
    let items: [WatchGroceryItem]

    static let empty = WatchSnapshot(lists: [], items: [])
}

struct WatchGroceryList: Codable, Identifiable, Hashable {
    let id: Int64
    let name: String
    let isShared: Bool
}

struct WatchGroceryItem: Codable, Identifiable, Hashable {
    let id: Int64
    let listId: Int64
    let name: String
    let quantity: String?
    let category: String
    let isChecked: Bool

    func toggled() -> WatchGroceryItem {
        WatchGroceryItem(
            id: id, listId: listId, name: name, quantity: quantity, category: category,
            isChecked: !isChecked
        )
    }
}
