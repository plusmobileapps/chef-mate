package com.plusmobileapps.chefmate.auth.data.impl

/**
 * Platform-specific Google Sign-In entry point. Each actual obtains a Google ID token using the
 * native flow appropriate for the target (Credential Manager on Android, GoogleSignIn-iOS via a
 * Swift bridge on iOS, system-browser + loopback HTTP server on JVM desktop).
 *
 * The raw nonce is generated inside the actual and surfaced alongside the ID token so the caller
 * can hand both to `supabaseClient.auth.signInWith(IDToken)` — Supabase verifies that the nonce
 * embedded (as SHA-256) inside the ID token matches.
 */
expect class GoogleSignInProvider {
    suspend fun signIn(): GoogleSignInResult
}

data class GoogleSignInResult(val idToken: String, val rawNonce: String)

sealed class GoogleSignInException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {
    class Cancelled : GoogleSignInException("Google sign-in was cancelled")

    class NotConfigured(message: String) : GoogleSignInException(message)

    class Failed(message: String, cause: Throwable? = null) : GoogleSignInException(message, cause)
}
