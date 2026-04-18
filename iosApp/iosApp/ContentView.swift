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
    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate

    var body: some View {
        ComposeView(
            rootBloc: appDelegate.root,
            backDispatcher: appDelegate.backDispatcher
        )
        .ignoresSafeArea(.all)
        .ignoresSafeArea(.keyboard)
        .onOpenURL { url in
            guard url.scheme == "chefmate", url.host == "import",
                  let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
                  let sharedUrl = components.queryItems?.first(where: { $0.name == "url" })?.value
            else { return }
            appDelegate.root.handleSharedUrl(url: sharedUrl)
        }
    }
}



