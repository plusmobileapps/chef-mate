import SwiftUI

/// Second screen: view items for the selected list, toggle them, and add new ones.
struct GroceryItemsView: View {
    let listId: Int64
    let title: String

    @EnvironmentObject private var store: GroceryStore
    @State private var showingAdd = false

    private var items: [WatchGroceryItem] { store.items(for: listId) }

    var body: some View {
        List {
            Section {
                Button {
                    showingAdd = true
                } label: {
                    Label("Add item", systemImage: "plus.circle.fill")
                }
            }

            ForEach(items, id: \.id) { item in
                Button {
                    store.setChecked(itemId: item.id, isChecked: !item.isChecked)
                } label: {
                    HStack {
                        Image(systemName: item.isChecked ? "checkmark.circle.fill" : "circle")
                            .foregroundStyle(item.isChecked ? .green : .secondary)
                        VStack(alignment: .leading) {
                            Text(item.name)
                                .strikethrough(item.isChecked)
                            if let quantity = item.quantity, !quantity.isEmpty {
                                Text(quantity).font(.footnote).foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle(title)
        .sheet(isPresented: $showingAdd) {
            AddItemView { name in
                store.addItem(listId: listId, name: name)
            }
        }
    }
}
