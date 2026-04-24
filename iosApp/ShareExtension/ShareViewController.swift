import UIKit
import UniformTypeIdentifiers

class ShareViewController: UIViewController {

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        handleSharedURL()
    }

    private func handleSharedURL() {
        guard let item = extensionContext?.inputItems.first as? NSExtensionItem,
              let provider = item.attachments?.first(where: {
                  $0.hasItemConformingToTypeIdentifier(UTType.url.identifier)
              })
        else {
            close()
            return
        }

        provider.loadItem(forTypeIdentifier: UTType.url.identifier) { [weak self] item, _ in
            DispatchQueue.main.async {
                guard let url = item as? URL,
                      let encoded = url.absoluteString
                          .addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
                      let deepLink = URL(string: "chefmate://import?url=\(encoded)")
                else {
                    self?.close()
                    return
                }
                self?.open(deepLink)
                self?.close()
            }
        }
    }

    private func open(_ url: URL) {
        // Responder chain trick — extensions can't call UIApplication.shared.open directly
        var responder: UIResponder? = self as UIResponder
        let selector = NSSelectorFromString("openURL:")
        while let r = responder {
            if r.responds(to: selector) {
                r.perform(selector, with: url)
                return
            }
            responder = r.next
        }
    }

    private func close() {
        extensionContext?.completeRequest(returningItems: nil)
    }
}
