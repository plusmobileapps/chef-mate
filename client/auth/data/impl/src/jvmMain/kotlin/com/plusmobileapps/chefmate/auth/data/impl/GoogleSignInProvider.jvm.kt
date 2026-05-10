package com.plusmobileapps.chefmate.auth.data.impl

import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import java.awt.Desktop
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

actual class GoogleSignInProvider {

    actual suspend fun signIn(): GoogleSignInResult {
        val clientId = BuildConfig.GOOGLE_DESKTOP_CLIENT_ID
        val clientSecret = BuildConfig.GOOGLE_DESKTOP_CLIENT_SECRET
        if (clientId.isBlank()) {
            throw GoogleSignInException.NotConfigured(
                "GOOGLE_DESKTOP_CLIENT_ID is not set — add google.desktopClientId to local.properties."
            )
        }

        val codeVerifier = randomUrlSafeString(64)
        val codeChallenge = sha256Base64Url(codeVerifier)
        val state = randomUrlSafeString(32)
        val rawNonce = randomUrlSafeString(32)
        val hashedNonce = sha256Base64Url(rawNonce)

        val codeDeferred = CompletableDeferred<String>()
        val server = startCallbackServer(expectedState = state, codeDeferred = codeDeferred)
        val redirectUri = "http://127.0.0.1:${server.address.port}/callback"

        try {
            val authUrl =
                buildAuthorizationUrl(clientId, redirectUri, codeChallenge, state, hashedNonce)
            openInBrowser(authUrl)

            val code = withTimeout(AUTH_TIMEOUT_MS) { codeDeferred.await() }
            val idToken =
                exchangeCodeForIdToken(code, clientId, clientSecret, redirectUri, codeVerifier)
            return GoogleSignInResult(idToken = idToken, rawNonce = rawNonce)
        } finally {
            server.stop(0)
        }
    }

    private fun startCallbackServer(
        expectedState: String,
        codeDeferred: CompletableDeferred<String>,
    ): HttpServer {
        // Port 0 lets the OS pick any free port — Google allows arbitrary loopback ports.
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/callback") { exchange ->
            handleCallback(exchange, expectedState, codeDeferred)
        }
        server.executor = null
        server.start()
        return server
    }

    private fun handleCallback(
        exchange: HttpExchange,
        expectedState: String,
        codeDeferred: CompletableDeferred<String>,
    ) {
        val query = exchange.requestURI.rawQuery.orEmpty()
        val params = parseQuery(query)
        val error = params["error"]
        val state = params["state"]
        val code = params["code"]

        val (status, html) =
            when {
                error == "access_denied" -> {
                    codeDeferred.completeExceptionally(GoogleSignInException.Cancelled())
                    HttpStatusCode.OK.value to
                        closeWindowHtml("Sign-in cancelled. You can close this window.")
                }
                error != null -> {
                    codeDeferred.completeExceptionally(
                        GoogleSignInException.Failed("OAuth error: $error")
                    )
                    HttpStatusCode.OK.value to
                        closeWindowHtml("Sign-in failed. You can close this window.")
                }
                state != expectedState -> {
                    codeDeferred.completeExceptionally(
                        GoogleSignInException.Failed("OAuth state mismatch")
                    )
                    HttpStatusCode.BadRequest.value to closeWindowHtml("Invalid request.")
                }
                code.isNullOrBlank() -> {
                    codeDeferred.completeExceptionally(
                        GoogleSignInException.Failed("Missing authorization code in callback")
                    )
                    HttpStatusCode.BadRequest.value to closeWindowHtml("Invalid request.")
                }
                else -> {
                    codeDeferred.complete(code)
                    HttpStatusCode.OK.value to
                        closeWindowHtml("Signed in. You can close this window.")
                }
            }

        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders["Content-Type"] = listOf("text/html; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use {
            OutputStreamWriter(it, StandardCharsets.UTF_8).apply {
                write(html)
                flush()
            }
        }
    }

    private fun buildAuthorizationUrl(
        clientId: String,
        redirectUri: String,
        codeChallenge: String,
        state: String,
        hashedNonce: String,
    ): String {
        val params =
            mapOf(
                "client_id" to clientId,
                "redirect_uri" to redirectUri,
                "response_type" to "code",
                "scope" to "openid email profile",
                "code_challenge" to codeChallenge,
                "code_challenge_method" to "S256",
                "state" to state,
                "nonce" to hashedNonce,
                "access_type" to "offline",
                "prompt" to "select_account",
            )
        val query = params.entries.joinToString("&") { (k, v) -> "${urlEncode(k)}=${urlEncode(v)}" }
        return "https://accounts.google.com/o/oauth2/v2/auth?$query"
    }

    private suspend fun exchangeCodeForIdToken(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        codeVerifier: String,
    ): String {
        val client = HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        try {
            val formParams = Parameters.build {
                append("client_id", clientId)
                if (clientSecret.isNotBlank()) append("client_secret", clientSecret)
                append("code", code)
                append("code_verifier", codeVerifier)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
            }
            val response: HttpResponse =
                client.post("https://oauth2.googleapis.com/token") {
                    setBody(FormDataContent(formParams))
                }
            if (!response.status.isSuccess()) {
                val body = response.body<String>()
                throw GoogleSignInException.Failed(
                    "Token exchange failed: ${response.status}: $body"
                )
            }
            val tokenResponse: GoogleTokenResponse = response.body()
            return tokenResponse.idToken
                ?: throw GoogleSignInException.Failed("Token exchange response missing id_token")
        } finally {
            client.close()
        }
    }

    private fun openInBrowser(url: String) {
        try {
            if (
                Desktop.isDesktopSupported() &&
                    Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            ) {
                Desktop.getDesktop().browse(URI(url))
                return
            }
        } catch (_: Exception) {
            // Fall through to the manual-instruction path below.
        }
        throw GoogleSignInException.Failed(
            "Couldn't open the system browser. Open this URL manually:\n$url"
        )
    }

    private fun sha256Base64Url(input: String): String {
        val digest =
            MessageDigest.getInstance("SHA-256").digest(input.toByteArray(StandardCharsets.UTF_8))
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    private fun randomUrlSafeString(byteLen: Int): String {
        val bytes = ByteArray(byteLen)
        SecureRandom().nextBytes(bytes)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun parseQuery(query: String): Map<String, String> =
        query
            .split("&")
            .mapNotNull {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) parts[0] to urlDecode(parts[1]) else null
            }
            .toMap()

    private fun urlEncode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20")

    private fun urlDecode(value: String): String =
        java.net.URLDecoder.decode(value, StandardCharsets.UTF_8)

    private fun closeWindowHtml(message: String): String =
        """
        <!doctype html>
        <html><head><title>Chef Mate</title><meta charset="utf-8" /></head>
        <body style="font-family:-apple-system,Segoe UI,sans-serif;padding:48px;text-align:center;">
        <h2>$message</h2>
        <script>window.close()</script>
        </body></html>
        """
            .trimIndent()

    @Serializable
    private data class GoogleTokenResponse(
        val access_token: String? = null,
        val id_token: String? = null,
        val expires_in: Int? = null,
        val token_type: String? = null,
        val refresh_token: String? = null,
        val scope: String? = null,
    ) {
        val idToken: String?
            get() = id_token
    }

    private companion object {
        const val AUTH_TIMEOUT_MS = 5L * 60L * 1000L
    }
}
