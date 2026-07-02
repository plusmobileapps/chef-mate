package com.plusmobileapps.chefmate

import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

/**
 * iOS-side bridge that lets the SwiftUI phone app hand its Supabase session to the watch over
 * WatchConnectivity. The phone owns the refresh token and drives refresh; the watch only ever
 * receives short-lived access tokens (via [currentTokens]), so the shared token chain is never
 * rotated from two devices. Exposed on [RootBlocProvider] from the DI graph.
 */
@Inject
@SingleIn(AppScope::class)
class WatchSessionRelay(
    private val authRepository: AuthenticationRepository,
    @Main mainContext: CoroutineContext,
) {
    private val scope = CoroutineScope(SupervisorJob() + mainContext)

    /**
     * Invokes [onChange] whenever the auth state changes (sign in / out) — not on the initial
     * value, so the caller controls the first push. Returns a function that cancels the observer.
     */
    fun observeAuthChanges(onChange: () -> Unit): () -> Unit {
        val job = scope.launch { authRepository.state.drop(1).collect { onChange() } }
        return { job.cancel() }
    }

    /**
     * Reads the freshest access token (the phone auto-refreshes) and delivers it to [onResult].
     * Passed as primitives — not the `SessionTokens` type, which isn't exported by the framework.
     * [accessToken] is `null` when signed out.
     */
    fun currentTokens(onResult: (accessToken: String?, expiresAtEpochSeconds: Long) -> Unit) {
        scope.launch {
            val tokens = authRepository.currentSessionTokens()
            onResult(tokens?.accessToken, tokens?.expiresAtEpochSeconds ?: 0L)
        }
    }
}
