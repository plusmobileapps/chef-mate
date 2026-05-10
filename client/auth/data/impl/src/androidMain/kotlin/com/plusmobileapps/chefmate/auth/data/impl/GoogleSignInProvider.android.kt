package com.plusmobileapps.chefmate.auth.data.impl

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import java.security.MessageDigest
import java.util.UUID

actual class GoogleSignInProvider(
    private val appContext: Context,
    private val activityHolder: CurrentActivityHolder,
) {
    actual suspend fun signIn(): GoogleSignInResult {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        if (webClientId.isBlank()) {
            throw GoogleSignInException.NotConfigured(
                "GOOGLE_WEB_CLIENT_ID is not set — add google.webClientId to local.properties."
            )
        }

        val activity =
            activityHolder.current
                ?: throw GoogleSignInException.Failed(
                    "No resumed Activity available for Credential Manager"
                )

        val rawNonce = UUID.randomUUID().toString()
        val hashedNonce = sha256Hex(rawNonce)

        val googleIdOption =
            GetGoogleIdOption.Builder()
                // Show all Google accounts on the device, not just ones that have signed in here.
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false)
                .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        val credentialManager = CredentialManager.create(appContext)

        val response =
            try {
                credentialManager.getCredential(context = activity, request = request)
            } catch (e: GetCredentialCancellationException) {
                throw GoogleSignInException.Cancelled()
            } catch (e: NoCredentialException) {
                throw GoogleSignInException.Failed("No Google account available on this device", e)
            } catch (e: GetCredentialException) {
                throw GoogleSignInException.Failed(e.message ?: "Credential Manager failed", e)
            }

        val credential =
            try {
                GoogleIdTokenCredential.createFrom(response.credential.data)
            } catch (e: GoogleIdTokenParsingException) {
                throw GoogleSignInException.Failed("Failed to parse Google ID token", e)
            }

        return GoogleSignInResult(idToken = credential.idToken, rawNonce = rawNonce)
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
