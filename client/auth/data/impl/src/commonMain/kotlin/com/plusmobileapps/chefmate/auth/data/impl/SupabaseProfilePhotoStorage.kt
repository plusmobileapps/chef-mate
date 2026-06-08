@file:OptIn(ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.auth.data.impl

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ProfilePhotoStorage
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseProfilePhotoStorage(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthenticationRepository,
) : ProfilePhotoStorage {

    override suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String {
        // A real user is always signed in by the time they reach the profile screen, but keep the
        // same lazy session guarantee as recipe photos so the call never races a missing session.
        authRepository.ensureSession().getOrElse {
            throw IllegalStateException(
                "Cannot upload avatar: failed to acquire Supabase session",
                it,
            )
        }
        val ownerFolder =
            (authRepository.state.value as? AuthState.Authenticated)?.user?.userId
                ?: error("Cannot upload avatar: no Supabase session")
        val bucket = supabaseClient.storage.from(BUCKET_NAME)
        val sanitizedExtension = fileExtension.trimStart('.').lowercase().ifBlank { "jpg" }
        // Randomize the filename so caches (Supabase CDN, Coil) don't serve a stale avatar after an
        // update; the old object is cleaned up best-effort by callers via deletePhoto.
        val path = "$ownerFolder/${Uuid.random()}.$sanitizedExtension"
        bucket.upload(path = path, data = bytes) { upsert = true }
        return bucket.publicUrl(path)
    }

    override suspend fun deletePhoto(publicUrl: String) {
        val needle = "/$BUCKET_NAME/"
        if (!publicUrl.contains(needle)) return
        val path = publicUrl.substringAfter(needle)
        if (path.isBlank()) return
        try {
            supabaseClient.storage.from(BUCKET_NAME).delete(path)
        } catch (t: Throwable) {
            Logger.w(throwable = t, tag = "SupabaseProfilePhotoStorage") {
                "Failed to delete avatar at $path"
            }
        }
    }

    private companion object {
        const val BUCKET_NAME = "avatars"
    }
}
