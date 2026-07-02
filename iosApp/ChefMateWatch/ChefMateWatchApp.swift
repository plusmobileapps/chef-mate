import SwiftUI

@main
struct ChefMateWatchApp: App {
    @StateObject private var store = GroceryStore()
    @Environment(\.scenePhase) private var scenePhase

    /// Retained for the app's lifetime so the watch keeps receiving the Supabase session handed
    /// off from the iPhone over WatchConnectivity.
    private let connectivity: WatchConnectivityManager

    init() {
        let store = GroceryStore()
        _store = StateObject(wrappedValue: store)
        connectivity = WatchConnectivityManager(store: store)
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active { connectivity.requestSession() }
        }
    }
}

struct RootView: View {
    @EnvironmentObject private var store: GroceryStore

    var body: some View {
        NavigationStack {
            if store.isSignedIn {
                GroceryListsView()
            } else {
                SignedOutView()
            }
        }
        .task { await store.syncNow() }
    }
}

struct SignedOutView: View {
    var body: some View {
        ContentUnavailableView(
            "Sign in on iPhone",
            systemImage: "iphone.and.arrow.forward",
            description: Text("Open Chef Mate on your iPhone to sync your grocery lists to your watch.")
        )
    }
}
