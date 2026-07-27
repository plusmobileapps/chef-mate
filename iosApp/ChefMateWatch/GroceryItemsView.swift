import SwiftUI

/// Second screen: view items for the selected list grouped by aisle (with sticky category headers,
/// mirroring the Compose grocery list), toggle them, and add new ones.
struct GroceryItemsView: View {
    let listId: Int64
    let title: String

    @EnvironmentObject private var store: GroceryStore
    @State private var showingAdd = false
    @State private var showingFilter = false

    /// Persisted like the phone's filter preference, and shared across lists for the same reason:
    /// the choice is about how you shop, not about one particular list.
    @AppStorage("groceryFilter") private var filter: WatchGroceryFilter = .all

    private var groups: [WatchGroceryGroup] {
        store.items(for: listId).filtered(by: filter).groupedByAisle()
    }

    var body: some View {
        // The list extends edge-to-edge horizontally so category headers can run the full width of
        // the screen; `hInset` is the horizontal safe-area inset we then re-apply to the actual
        // content (row + header text) so text still clears the display's rounded corners.
        GeometryReader { proxy in
            let hInset = max(proxy.safeAreaInsets.leading, proxy.safeAreaInsets.trailing, 8)
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 6, pinnedViews: [.sectionHeaders]) {
                    AddItemRow { showingAdd = true }
                        .padding(.horizontal, hInset)
                        .padding(.top, 2)

                    if groups.isEmpty {
                        Text(filter.emptyMessage)
                            .font(.footnote)
                            .foregroundStyle(WatchTheme.onSurfaceVariant)
                            .padding(.horizontal, hInset)
                            .padding(.top, 6)
                            .transition(.opacity)
                    }

                    ForEach(groups) { group in
                        Section {
                            ForEach(Array(group.items.enumerated()), id: \.element.id) { index, item in
                                GroceryItemRow(item: item) {
                                    store.setChecked(itemId: item.id, isChecked: !item.isChecked)
                                }
                                .padding(.horizontal, hInset)
                                .transition(.itemRemoval)
                                if index < group.items.count - 1 {
                                    Divider().padding(.leading, hInset).transition(.opacity)
                                }
                            }
                        } header: {
                            CategoryHeader(title: group.title, textInset: hInset)
                                .transition(.opacity)
                        }
                    }
                }
                .padding(.bottom, 8)
                // Scoped to `groups` so only list changes animate — a filter switch, a phone
                // snapshot, or an item leaving because it was just checked off. Rows below the
                // departing one slide up to close the gap.
                .animation(.snappy, value: groups)
            }
        }
        .ignoresSafeArea(.container, edges: .horizontal)
        .navigationTitle(title)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showingFilter = true
                } label: {
                    Image(
                        systemName: filter == .all
                            ? "line.3.horizontal.decrease.circle"
                            : "line.3.horizontal.decrease.circle.fill"
                    )
                }
                .foregroundStyle(filter == .all ? WatchTheme.onSurfaceVariant : WatchTheme.primary)
                .accessibilityLabel("Filter: \(filter.displayName)")
            }
        }
        .sheet(isPresented: $showingAdd) {
            AddItemView { name in
                store.addItem(listId: listId, name: name)
            }
        }
        .sheet(isPresented: $showingFilter) {
            GroceryFilterView(selection: $filter)
        }
    }
}

extension AnyTransition {
    /// How a row enters and leaves the list. Departures slide off toward the leading edge as they
    /// fade — the item is being struck off — while arrivals just fade in, so a newly added item
    /// doesn't fly across the screen on its way to the bottom.
    static var itemRemoval: AnyTransition {
        .asymmetric(
            insertion: .opacity,
            removal: .move(edge: .leading).combined(with: .opacity)
        )
    }
}

/// Filter picker, mirroring the options in the phone's Sort & Filter sheet.
private struct GroceryFilterView: View {
    @Binding var selection: WatchGroceryFilter

    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            List(WatchGroceryFilter.allCases) { option in
                Button {
                    selection = option
                    dismiss()
                } label: {
                    HStack(spacing: 8) {
                        Text(option.displayName)
                            .foregroundStyle(WatchTheme.onSurface)
                        Spacer(minLength: 0)
                        if option == selection {
                            Image(systemName: "checkmark")
                                .foregroundStyle(WatchTheme.primary)
                        }
                    }
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
            }
            .listStyle(.plain)
            .navigationTitle("Filter")
        }
        .tint(WatchTheme.primary)
    }
}

/// Sticky section header styled like the Compose `CategoryHeader`: a surfaceVariant bar that spans
/// the full screen width, with the label inset by the safe area so it lines up with the item text.
private struct CategoryHeader: View {
    let title: String
    let textInset: CGFloat

    var body: some View {
        Text(title)
            .font(.footnote.weight(.semibold))
            .foregroundStyle(WatchTheme.onSurfaceVariant)
            .padding(.horizontal, textInset)
            .padding(.vertical, 5)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(WatchTheme.surfaceVariant)
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
                    .contentTransition(.symbolEffect(.replace))
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
            .padding(.vertical, 4)
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
