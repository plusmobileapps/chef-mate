import UIKit
import UniformTypeIdentifiers

class ShareViewController: UIViewController {

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        handleSharedURL()
    }

    private func handleSharedURL() {
        guard let item = extensionContext?.inputItems.first as? NSExtensionItem,
              let attachments = item.attachments
        else {
            close()
            return
        }

        // Try URL type first, then fall back to plain text (some apps share URLs as text)
        if let provider = attachments.first(where: { $0.hasItemConformingToTypeIdentifier(UTType.url.identifier) }) {
            provider.loadItem(forTypeIdentifier: UTType.url.identifier) { [weak self] item, _ in
                DispatchQueue.main.async {
                    if let url = item as? URL {
                        self?.openInApp(url.absoluteString)
                    } else {
                        self?.close()
                    }
                }
            }
        } else if let provider = attachments.first(where: { $0.hasItemConformingToTypeIdentifier(UTType.plainText.identifier) }) {
            provider.loadItem(forTypeIdentifier: UTType.plainText.identifier) { [weak self] item, _ in
                DispatchQueue.main.async {
                    if let text = item as? String, text.hasPrefix("http") {
                        self?.openInApp(text)
                    } else {
                        self?.close()
                    }
                }
            }
        } else {
            close()
        }
    }

    private func openInApp(_ urlString: String) {
        guard let encoded = urlString.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
              let deepLink = URL(string: "chefmate://import?url=\(encoded)")
        else {
            close()
            return
        }

        // Access UIApplication.shared.open(_:options:completionHandler:) indirectly.
        // The class exists in extensions but is marked unavailable at compile time.
        guard let applicationClass = NSClassFromString("UIApplication") as? NSObject.Type,
              let application = applicationClass.perform(NSSelectorFromString("sharedApplication"))?.takeUnretainedValue()
        else {
            close()
            return
        }

        let selector = NSSelectorFromString("openURL:options:completionHandler:")
        let imp = application.method(for: selector)
        typealias OpenURLFunc = @convention(c) (AnyObject, Selector, URL, [String: Any], Any?) -> Void
        let openURL = unsafeBitCast(imp, to: OpenURLFunc.self)
        openURL(application, selector, deepLink, [:], nil)

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak self] in
            self?.close()
        }
    }

    private func close() {
        extensionContext?.completeRequest(returningItems: nil)
    }
}
