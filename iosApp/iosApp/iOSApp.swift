import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    lazy var root: RootBloc = RootBlocKt.buildRootBloc(
        componentContext: DefaultComponentContext(
            lifecycle: ApplicationLifecycle(),
            stateKeeper: nil,
            instanceKeeper: nil,
            backHandler: nil
        )
    )
}
