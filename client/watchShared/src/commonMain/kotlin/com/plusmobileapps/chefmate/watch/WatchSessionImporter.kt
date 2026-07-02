package com.plusmobileapps.chefmate.watch

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import kotlin.time.Clock

/**
 * Adopts a short-lived Supabase session handed off from the phone (over WatchConnectivity).
 * Extracted behind an interface so [WatchGroceryController] doesn't depend on the Supabase SDK
 * directly and can be unit-tested without a real client.
 *
 * The watch receives only the **access token** — the phone owns the refresh token and drives
 * refresh, so the watch never rotates the shared token chain (which would sign the phone out). The
 * session is imported with `autoRefresh = false`; when it expires the watch pulls a fresh access
 * token from the phone.
 */
interface WatchSessionImporter {
    suspend fun importSession(accessToken: String, expiresAtEpochSeconds: Long)
}

@Inject
@ContributesBinding(AppScope::class)
class SupabaseWatchSessionImporter(private val supabaseClient: SupabaseClient) :
    WatchSessionImporter {
    override suspend fun importSession(accessToken: String, expiresAtEpochSeconds: Long) {
        val expiresIn = (expiresAtEpochSeconds - Clock.System.now().epochSeconds).coerceAtLeast(0)
        supabaseClient.auth.importSession(
            UserSession(
                accessToken = accessToken,
                refreshToken = "",
                expiresIn = expiresIn,
                tokenType = "bearer",
            ),
            autoRefresh = false,
        )
    }
}
