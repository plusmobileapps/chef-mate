package com.plusmobileapps.chefmate.profile.data.impl

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.profile.data.ProfileHandle
import com.plusmobileapps.chefmate.profile.data.ProfileRepository
import com.plusmobileapps.chefmate.profile.data.SocialProfile
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseProfileRepository(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthenticationRepository,
) : ProfileRepository {

    override suspend fun getMyProfile(): Result<SocialProfile?> = runCatching {
        val userId = requireRealUserId()
        supabaseClient.postgrest
            .rpc(
                "get_profile_by_id",
                buildJsonObject { put("p_profile_id", JsonPrimitive(userId)) },
            )
            .decodeList<RemoteProfile>()
            .firstOrNull()
            ?.toSocialProfile()
    }

    override suspend fun getProfileByHandle(handle: String): Result<SocialProfile?> = runCatching {
        val normalized = ProfileHandle.normalize(handle)
        // Nothing that can't be a handle is worth a round-trip; the RPC would return no row anyway.
        if (!ProfileHandle.isValidFormat(normalized)) return@runCatching null
        supabaseClient.postgrest
            .rpc(
                "get_profile_by_handle",
                buildJsonObject { put("p_handle", JsonPrimitive(normalized)) },
            )
            .decodeList<RemoteProfile>()
            .firstOrNull()
            ?.toSocialProfile()
    }

    override suspend fun isHandleAvailable(handle: String): Result<Boolean> = runCatching {
        val normalized = ProfileHandle.normalize(handle)
        if (!ProfileHandle.isValidFormat(normalized)) return@runCatching false
        supabaseClient.postgrest
            .rpc(
                "is_handle_available",
                buildJsonObject { put("p_handle", JsonPrimitive(normalized)) },
            )
            .decodeAs<Boolean>()
    }

    override suspend fun claimHandle(
        handle: String,
        displayName: String,
        bio: String,
        avatarUrl: String?,
    ): Result<SocialProfile> = runCatching {
        val userId = requireRealUserId()
        val normalized = ProfileHandle.normalize(handle)
        if (!ProfileHandle.isValidFormat(normalized)) {
            throw ProfileRepository.HandleRejected(normalized)
        }
        try {
            supabaseClient
                .from("profiles")
                .insert(
                    ProfileUpsert(
                        id = userId,
                        handle = normalized,
                        displayName = displayName.trim(),
                        bio = bio.trim(),
                        avatarUrl = avatarUrl,
                    )
                ) {
                    select()
                }
                .decodeSingle<RemoteProfile>()
                .toSocialProfile()
        } catch (e: PostgrestRestException) {
            // The availability pre-check is advisory only — two users can race to the same handle,
            // and the UNIQUE index is what actually decides. Translate the SQLSTATEs the claim path
            // can legitimately produce so the UI can say something useful instead of "unknown
            // error"; anything else is a real failure and propagates.
            throw when (e.code) {
                UNIQUE_VIOLATION -> ProfileRepository.HandleTaken(normalized)
                CHECK_VIOLATION -> ProfileRepository.HandleRejected(normalized)
                else -> e
            }
        }
    }

    override suspend fun updateProfile(
        displayName: String,
        bio: String,
        avatarUrl: String?,
    ): Result<SocialProfile> = runCatching {
        val userId = requireRealUserId()
        supabaseClient
            .from("profiles")
            .update({
                set("display_name", displayName.trim())
                set("bio", bio.trim())
                // Mirrors AuthenticationRepository.updateProfile: a null avatar means "unchanged",
                // not "clear it", so a save that didn't pick a new photo keeps the existing one.
                if (avatarUrl != null) set("avatar_url", avatarUrl)
            }) {
                select()
                filter { eq("id", userId) }
            }
            .decodeSingle<RemoteProfile>()
            .toSocialProfile()
    }

    /**
     * The signed-in user's id, rejecting anonymous sessions. A profile is a durable public identity
     * and anonymous sessions have no credentials to come back to, so letting one claim a handle
     * would strand that handle on an account nobody can ever sign into again.
     */
    private fun requireRealUserId(): String {
        val user =
            (authRepository.state.value as? AuthState.Authenticated)?.user
                ?: error("Cannot read or write a profile while signed out")
        check(!user.isAnonymous) { "Anonymous users cannot have a public profile" }
        return user.userId
    }

    private companion object {
        const val UNIQUE_VIOLATION = "23505"
        const val CHECK_VIOLATION = "23514"
    }
}
