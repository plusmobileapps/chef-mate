import SwiftUI

/// First screen: pick a grocery list.
struct GroceryListsView: View {
    @EnvironmentObject private var store: GroceryStore

    var body: some View {
        List {
            if store.lists.isEmpty {
                Text("No grocery lists yet.")
                    .foregroundStyle(WatchTheme.onSurfaceVariant)
            } else {
                ForEach(store.lists, id: \.id) { list in
                    NavigationLink(value: list.id) {
                        Label {
                            Text(list.name).foregroundStyle(WatchTheme.onSurface)
                        } icon: {
                            Image(systemName: list.isShared ? "person.2.fill" : "cart.fill")
                                .foregroundStyle(WatchTheme.primary)
                        }
                    }
                }
            }
        }
        .listStyle(.plain)
        .navigationTitle("Lists")
        .navigationDestination(for: Int64.self) { listId in
            GroceryItemsView(
                listId: listId,
                title: store.lists.first { $0.id == listId }?.name ?? "Groceries"
            )
        }
    }
}
