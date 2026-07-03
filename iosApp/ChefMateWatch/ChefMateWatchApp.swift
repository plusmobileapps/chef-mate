import SwiftUI

@main
struct ChefMateWatchApp: App {
    @StateObject private var store = GroceryStore()
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { store.refresh() }
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var store: GroceryStore

    var body: some View {
        NavigationStack {
            if !store.hasLoaded && store.lists.isEmpty {
                ContentUnavailableView(
                    "Open Chef Mate on iPhone",
                    systemImage: "iphone.and.arrow.forward",
                    description: Text("Your grocery lists sync from your iPhone.")
                )
            } else {
                GroceryListsView()
            }
        }
        .task { store.refresh() }
    }
}
