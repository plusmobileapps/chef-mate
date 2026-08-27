package com.plusmobileapps.chefmate.auth.data.impl

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodedPath
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Renews the access token and replays the request whenever Supabase rejects one as unauthorized.
 *
 * The SDK's auto-refresh is a single in-process timer, and outside Android nothing re-arms it when
 * the machine sleeps through its window. A 401 is the only signal that doesn't depend on that timer
 * — or on the client clock being right — so it's the one worth acting on.
 *
 * This sits below every repository, which matters: the sync paths swallow per-item failures to keep
 * one bad row from stopping a whole reconcile, so a 401 surfaced any higher would be caught and
 * dropped before anything could react to it. Down here the request simply succeeds on the retry.
 */
internal class ExpiredTokenRetryConfig {
    /**
     * Renews the session and returns a usable token, or null if it couldn't be renewed — in which
     * case the original 401 stands rather than a second doomed request being sent.
     *
     * Receives the token the rejected request carried, so an implementation can tell "nobody has
     * refreshed yet" from "someone already did". See [ConcurrentRefreshGuard].
     */
    var refreshToken: suspend (usedToken: String?) -> String? = { null }
}

internal val ExpiredTokenRetry =
    createClientPlugin("ExpiredTokenRetry", ::ExpiredTokenRetryConfig) {
        val refreshToken = pluginConfig.refreshToken
        on(Send) { request ->
            val call = proceed(request)
            if (call.response.status != HttpStatusCode.Unauthorized) return@on call
            // The token endpoint answers a dead refresh token with a 401 of its own. Retrying that
            // is how one expired session turns into an infinite refresh loop.
            if (request.url.encodedPath.startsWith(AUTH_PATH)) return@on call

            val usedToken = request.headers[HttpHeaders.Authorization]?.removePrefix(BEARER_PREFIX)
            val freshToken = refreshToken(usedToken) ?: return@on call
            // Replace rather than append: bearerAuth() would add a second Authorization header.
            request.headers[HttpHeaders.Authorization] = BEARER_PREFIX + freshToken
            proceed(request)
        }
    }

/**
 * Collapses a burst of 401s into a single refresh.
 *
 * A full sync fires many requests at once, so a dead token produces many simultaneous 401s. Letting
 * each one refresh independently is not merely wasteful: Supabase rotates the refresh token on
 * every use, so parallel refreshes look like token replay and can invalidate the session outright —
 * taking out the sign-in this code is trying to rescue.
 *
 * Callers hand over the token their request actually used. If the current token has moved on, some
 * other caller already refreshed and that result is reused; otherwise this one refreshes, under a
 * lock, while the rest wait.
 */
internal class ConcurrentRefreshGuard(
    private val currentToken: () -> String?,
    private val refresh: suspend () -> Unit,
) {
    private val mutex = Mutex()

    suspend fun tokenAfterRefresh(usedToken: String?): String? = mutex.withLock {
        val current = currentToken()
        if (current != null && current != usedToken) return@withLock current
        val refreshed =
            try {
                refresh()
                true
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                Logger.w(throwable = t, tag = TAG) {
                    "Refresh after a 401 failed; leaving the request to fail"
                }
                false
            }
        if (refreshed) currentToken() else null
    }

    private companion object {
        const val TAG = "ConcurrentRefreshGuard"
    }
}

private const val AUTH_PATH = "/auth/v1"
private const val BEARER_PREFIX = "Bearer "
