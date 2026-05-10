import Foundation
import UIKit
import ComposeApp
import CryptoKit
import GoogleSignIn

/// Implements the Kotlin `IosGoogleSignInBridge` contract by driving GoogleSignIn-iOS.
/// Generates the nonce on the Swift side (CryptoKit gives us SHA-256 for free) so the Kotlin
/// actual stays free of cinterop / native crypto code.
final class GoogleSignInBridge: NSObject, IosGoogleSignInBridge {
    func signIn() async throws -> IosGoogleSignInResponse {
        guard let presenter = Self.topViewController() else {
            throw NSError(
                domain: "GoogleSignInBridge",
                code: -1,
                userInfo: [NSLocalizedDescriptionKey: "No view controller to present from"]
            )
        }

        let rawNonce = Self.randomNonceString()
        let hashedNonce = Self.sha256(rawNonce)

        let result = try await GIDSignIn.sharedInstance.signIn(
            withPresenting: presenter,
            hint: nil,
            additionalScopes: nil,
            nonce: hashedNonce
        )

        guard let idToken = result.user.idToken?.tokenString else {
            throw NSError(
                domain: "GoogleSignInBridge",
                code: -2,
                userInfo: [NSLocalizedDescriptionKey: "Google sign-in returned no ID token"]
            )
        }

        return IosGoogleSignInResponse(idToken: idToken, rawNonce: rawNonce)
    }

    private static func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        let charset: [Character] =
            Array("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-._")
        var result = ""
        var remaining = length
        while remaining > 0 {
            var randoms = [UInt8](repeating: 0, count: 16)
            let status = SecRandomCopyBytes(kSecRandomDefault, randoms.count, &randoms)
            precondition(status == errSecSuccess)
            randoms.forEach { random in
                if remaining == 0 { return }
                if random < charset.count {
                    result.append(charset[Int(random)])
                    remaining -= 1
                }
            }
        }
        return result
    }

    private static func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashed = SHA256.hash(data: inputData)
        return hashed.compactMap { String(format: "%02x", $0) }.joined()
    }

    private static func topViewController(
        from root: UIViewController? = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first(where: { $0.isKeyWindow })?.rootViewController
    ) -> UIViewController? {
        if let nav = root as? UINavigationController {
            return topViewController(from: nav.visibleViewController)
        }
        if let tab = root as? UITabBarController, let selected = tab.selectedViewController {
            return topViewController(from: selected)
        }
        if let presented = root?.presentedViewController {
            return topViewController(from: presented)
        }
        return root
    }
}
