import Foundation
import SwiftUI

/// Native mirror of the JSON snapshot the phone sends over WatchConnectivity. The watch holds no
/// Kotlin/Supabase — the phone (source of truth) serializes its grocery data into this shape.
struct WatchSnapshot: Codable {
    let lists: [WatchGroceryList]
    let items: [WatchGroceryItem]

    static let empty = WatchSnapshot(lists: [], items: [])

    /// Re-applies locally-made checks that the phone hasn't confirmed yet (see
    /// `GroceryStore.pendingChecks`).
    func applyingChecked(_ overrides: [Int64: Bool]) -> WatchSnapshot {
        guard !overrides.isEmpty else { return self }
        return WatchSnapshot(
            lists: lists,
            items: items.map { item in
                guard let isChecked = overrides[item.id] else { return item }
                return item.settingChecked(isChecked)
            }
        )
    }
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

    func settingChecked(_ isChecked: Bool) -> WatchGroceryItem {
        WatchGroceryItem(
            id: id, listId: listId, name: name, quantity: quantity, category: category,
            isChecked: isChecked
        )
    }
}

// MARK: - Material Expressive theme

/// Colours lifted from the shared Compose `ChefMateTheme` dark palette so the watch reads as the
/// same app as the phone/desktop/web clients (`purple200` primary, `teal200` secondary, plus the
/// Material 3 dark neutrals the scheme fills in). watchOS is always dark, so we only need the dark
/// values.
enum WatchTheme {
    static let primary = Color(red: 0xBB / 255, green: 0x86 / 255, blue: 0xFC / 255) // purple200
    static let secondary = Color(red: 0x03 / 255, green: 0xDA / 255, blue: 0xC5 / 255) // teal200
    static let surfaceVariant = Color(red: 0x49 / 255, green: 0x45 / 255, blue: 0x4F / 255)
    static let onSurface = Color(red: 0xE6 / 255, green: 0xE1 / 255, blue: 0xE5 / 255)
    static let onSurfaceVariant = Color(red: 0xCA / 255, green: 0xC4 / 255, blue: 0xD0 / 255)
    static let primaryContainer = Color(red: 0x4F / 255, green: 0x37 / 255, blue: 0x8B / 255)
}

// MARK: - Categories (mirrors the shared `GroceryCategory` enum + `displayName()`)

/// A category as it arrives from the phone (the Kotlin `GroceryCategory.name`), paired with the
/// display label and aisle order used by the Compose clients so the watch groups items identically.
enum WatchGroceryCategory: String, CaseIterable {
    case produce = "PRODUCE"
    case dairy = "DAIRY"
    case meat = "MEAT"
    case bakery = "BAKERY"
    case frozen = "FROZEN"
    case cannedGoods = "CANNED_GOODS"
    case condiments = "CONDIMENTS"
    case snacks = "SNACKS"
    case beverages = "BEVERAGES"
    case grains = "GRAINS"
    case baking = "BAKING"
    case spices = "SPICES"
    case other = "OTHER"

    var displayName: String {
        switch self {
        case .produce: return "Produce"
        case .dairy: return "Dairy"
        case .meat: return "Meat & Seafood"
        case .bakery: return "Bakery"
        case .frozen: return "Frozen"
        case .cannedGoods: return "Canned Goods"
        case .condiments: return "Condiments & Sauces"
        case .snacks: return "Snacks"
        case .beverages: return "Beverages"
        case .grains: return "Grains & Pasta"
        case .baking: return "Baking"
        case .spices: return "Spices & Seasonings"
        case .other: return "Other"
        }
    }
}

/// One aisle's worth of items, ready to render as a sticky-header section.
struct WatchGroceryGroup: Identifiable {
    let category: WatchGroceryCategory
    let items: [WatchGroceryItem]
    var id: String { category.rawValue }
    var title: String { category.displayName }
}

extension Array where Element == WatchGroceryItem {
    /// Groups items by aisle and orders the groups the way the phone does (by the shared enum's
    /// declaration order), preserving each item's incoming order within a group. Any unrecognised
    /// category falls into `OTHER`.
    func groupedByAisle() -> [WatchGroceryGroup] {
        let buckets = Dictionary(grouping: self) {
            WatchGroceryCategory(rawValue: $0.category) ?? .other
        }
        return WatchGroceryCategory.allCases.compactMap { category in
            guard let items = buckets[category], !items.isEmpty else { return nil }
            return WatchGroceryGroup(category: category, items: items)
        }
    }
}
