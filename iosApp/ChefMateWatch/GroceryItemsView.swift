import SwiftUI

/// Second screen: view items for the selected list grouped by aisle (with sticky category headers,
/// mirroring the Compose grocery list), toggle them, and add new ones.
struct GroceryItemsView: View {
    let listId: Int64
    let title: String

    @EnvironmentObject private var store: GroceryStore
    @State private var showingAdd = false

    private var groups: [WatchGroceryGroup] { store.items(for: listId).groupedByAisle() }

    var body: some View {
        List {
            Section {
                AddItemRow { showingAdd = true }
            }
            .listRowInsets(EdgeInsets())
            .listRowBackground(Color.clear)

            ForEach(groups) { group in
                Section {
                    ForEach(group.items, id: \.id) { item in
                        GroceryItemRow(item: item) {
                            store.setChecked(itemId: item.id, isChecked: !item.isChecked)
                        }
                    }
                } header: {
                    CategoryHeader(title: group.title)
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle(title)
        .sheet(isPresented: $showingAdd) {
            AddItemView { name in
                store.addItem(listId: listId, name: name)
            }
        }
    }
}

/// Sticky section header styled like the Compose `CategoryHeader` (surfaceVariant pill,
/// onSurfaceVariant label).
private struct CategoryHeader: View {
    let title: String

    var body: some View {
        Text(title)
            .font(.footnote.weight(.semibold))
            .foregroundStyle(WatchTheme.onSurfaceVariant)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(WatchTheme.surfaceVariant, in: RoundedRectangle(cornerRadius: 8))
    }
}

/// A single grocery row: a Material-style checkbox, the item name, and its quantity subtitle.
private struct GroceryItemRow: View {
    let item: WatchGroceryItem
    let onToggle: () -> Void

    var body: some View {
        Button(action: onToggle) {
            HStack(spacing: 10) {
                Image(systemName: item.isChecked ? "checkmark.square.fill" : "square")
                    .font(.title3)
                    .foregroundStyle(item.isChecked ? WatchTheme.primary : WatchTheme.onSurfaceVariant)
                VStack(alignment: .leading, spacing: 2) {
                    Text(item.name)
                        .foregroundStyle(item.isChecked ? WatchTheme.onSurfaceVariant : WatchTheme.onSurface)
                        .strikethrough(item.isChecked)
                    if let quantity = item.quantity, !quantity.isEmpty {
                        Text(quantity)
                            .font(.footnote)
                            .foregroundStyle(WatchTheme.onSurfaceVariant)
                    }
                }
                Spacer(minLength: 0)
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

/// Primary "Add item" affordance styled with the app's accent.
private struct AddItemRow: View {
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 8) {
                Image(systemName: "plus.circle.fill")
                Text("Add item").fontWeight(.medium)
                Spacer(minLength: 0)
            }
            .foregroundStyle(WatchTheme.primary)
            .padding(.vertical, 6)
            .padding(.horizontal, 10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(WatchTheme.primary.opacity(0.15), in: RoundedRectangle(cornerRadius: 12))
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
