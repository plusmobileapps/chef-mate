import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let rootBloc: RootBloc
    let backDispatcher: BackDispatcher
    
    func makeUIViewController(context: Context) -> UIViewController {
        let controller = MainViewController.shared.create(rootBloc: rootBloc, backDispatcher: backDispatcher)
        controller.overrideUserInterfaceStyle = .light
        return controller
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let appDelegate: AppDelegate

    var body: some View {
        ComposeView(
            rootBloc: appDelegate.root,
            backDispatcher: appDelegate.backDispatcher
        )
        .ignoresSafeArea(.all)
        .ignoresSafeArea(.keyboard)
        .onOpenURL { url in
            // The share extension re-enters via chefmate://import?url=… — hand that to the browser.
            if url.scheme == "chefmate", url.host == "import",
               let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
               let sharedUrl = components.queryItems?.first(where: { $0.name == "url" })?.value {
                appDelegate.root.handleSharedUrl(url: sharedUrl)
                return
            }
            // Any other custom-scheme deep link (e.g. chefmate://notifications) while running.
            if url.scheme == "chefmate" {
                appDelegate.root.handleDeepLink(url: url.absoluteString)
            }
        }
        // Universal Links (https://chefmate.plusmobileapps.com/…) arrive as a browsing user
        // activity, for both cold launch and while the app is already running.
        .onContinueUserActivity(NSUserActivityTypeBrowsingWeb) { activity in
            if let url = activity.webpageURL {
                appDelegate.root.handleDeepLink(url: url.absoluteString)
            }
        }
    }
}



