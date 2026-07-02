package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

/**
 * Adopts a Supabase session handed off from the phone (over WatchConnectivity). Extracted behind an
 * interface so [WatchGroceryController] doesn't depend on the Supabase SDK directly and can be
 * unit-tested without constructing a real client.
 */
interface WatchSessionImporter {
    suspend fun importSession(refreshToken: String)
}

@Inject
@ContributesBinding(AppScope::class)
class SupabaseWatchSessionImporter(private val supabaseClient: SupabaseClient) :
    WatchSessionImporter {
    /**
     * Refreshes from the transferred refresh token (yielding a fresh access token + user) and
     * imports it as the current session — which flips `AuthenticationRepository.state` to
     * Authenticated and triggers the repository's existing auth-state-driven sync.
     */
    override suspend fun importSession(refreshToken: String) {
        val session = supabaseClient.auth.refreshSession(refreshToken)
        supabaseClient.auth.importSession(session)
    }
}
