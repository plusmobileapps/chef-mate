package com.plusmobileapps.chefmate.auth.data.impl

/**
 * Swift-side hook. The iOS app registers an implementation at startup by setting
 * [IosGoogleSignInBridgeHolder.bridge]. Swift owns the entire flow — generating the raw nonce,
 * hashing it for GoogleSignIn-iOS, and returning the resulting ID token paired with the original
 * raw nonce so Supabase can verify.
 *
 * Keeping nonce generation on the Swift side avoids pulling cinterop / CryptoKit references into
 * Kotlin/Native; Swift already has CryptoKit for free.
 */
fun interface IosGoogleSignInBridge {
    @Throws(Exception::class) suspend fun signIn(): IosGoogleSignInResponse
}

data class IosGoogleSignInResponse(val idToken: String, val rawNonce: String)

object IosGoogleSignInBridgeHolder {
    // Set once at app startup by Swift; read by [GoogleSignInProvider.signIn]. Not contended.
    var bridge: IosGoogleSignInBridge? = null
}

actual class GoogleSignInProvider {
    actual suspend fun signIn(): GoogleSignInResult {
        val bridge =
            IosGoogleSignInBridgeHolder.bridge
                ?: throw GoogleSignInException.NotConfigured(
                    "IosGoogleSignInBridgeHolder.bridge was not set by the iOS app on startup"
                )

        return try {
            val response = bridge.signIn()
            GoogleSignInResult(idToken = response.idToken, rawNonce = response.rawNonce)
        } catch (e: GoogleSignInException) {
            throw e
        } catch (e: Exception) {
            throw GoogleSignInException.Failed(e.message ?: "Google sign-in failed", e)
        }
    }
}
