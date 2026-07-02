import WatchShared

/// The single `WatchGroceryController` for the watch process. The SwiftUI app and the Siri
/// `AddGroceryItemIntent` both use this instance so they share one SQLDelight database + sync
/// engine. Building the Metro graph is cheap but must happen exactly once.
enum SharedController {
    static let shared: WatchGroceryController = WatchEntryPointKt.createWatchGroceryController()
}
