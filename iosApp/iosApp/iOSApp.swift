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
    var backDispatcher: BackDispatcher = BackDispatcherKt.BackDispatcher()
    private var launchDeepLinkUrl: String?

    /// Hands the Supabase session to the watch companion. Lazily created after `root` builds the
    /// DI graph (which sets `RootBlocProvider.sessionRelay`).
    private lazy var watchSessionSender = WatchSessionSender(
        relay: RootBlocProvider.shared.sessionRelay
    )

    override init() {
        super.init()
        BugsnagStartup_iosKt.initializeBugsnag()
    }

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        if let url = launchOptions?[.url] as? URL,
           url.scheme == "chefmate",
           url.host != "import" {
            launchDeepLinkUrl = url.absoluteString
        }
        // Build the graph (which sets sessionRelay), then start the watch session bridge.
        _ = root
        _ = watchSessionSender
        return true
    }

    lazy var root: RootBloc = RootBlocProvider.shared.buildRootBloc(
        componentContext: DefaultComponentContext(
            lifecycle: ApplicationLifecycle(),
            stateKeeper: nil,
            instanceKeeper: nil,
            backHandler: backDispatcher
        ),
        application: UIApplication.shared,
        deepLinkUrl: launchDeepLinkUrl
    )
}
