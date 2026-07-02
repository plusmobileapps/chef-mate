import SwiftUI
import WatchShared

/// First screen: pick a grocery list.
struct GroceryListsView: View {
    @EnvironmentObject private var store: GroceryStore

    var body: some View {
        List {
            if store.lists.isEmpty {
                Text("No grocery lists yet.")
                    .foregroundStyle(.secondary)
            } else {
                ForEach(store.lists, id: \.id) { list in
                    NavigationLink(value: list.id) {
                        Label(list.name, systemImage: list.isShared ? "person.2" : "cart")
                    }
                }
            }
        }
        .navigationTitle("Lists")
        .navigationDestination(for: Int64.self) { listId in
            GroceryItemsView(
                listId: listId,
                title: store.lists.first { $0.id == listId }?.name ?? "Groceries"
            )
        }
        .task { await store.syncNow() }
    }
}
