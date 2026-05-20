@file:OptIn(ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.recipe.data.impl.remote

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.RecipePhotoStorage
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
class SupabaseRecipePhotoStorage(
    private val supabaseClient: SupabaseClient,
    private val authRepository: AuthenticationRepository,
) : RecipePhotoStorage {

    override suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String {
        // Lazily materialize a Supabase session before uploading. For first-time photo uploads
        // by a user who's been local-only up to now, this signs them in anonymously; for users
        // who already have a session (anon or real) this is a no-op. If ensureSession() fails
        // (e.g. offline at first photo), surface that so save() can show the upload error.
        authRepository.ensureSession().getOrElse {
            throw IllegalStateException(
                "Cannot upload photo: failed to acquire Supabase session",
                it,
            )
        }
        val ownerFolder =
            (authRepository.state.value as? AuthState.Authenticated)?.user?.userId
                ?: error("Cannot upload photo: no Supabase session")
        val bucket = supabaseClient.storage.from(BUCKET_NAME)
        val sanitizedExtension = fileExtension.trimStart('.').lowercase().ifBlank { "jpg" }
        val path = "$ownerFolder/${Uuid.random()}.$sanitizedExtension"
        bucket.upload(path = path, data = bytes) { upsert = false }
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
            Logger.w(throwable = t, tag = "SupabaseRecipePhotoStorage") {
                "Failed to delete photo at $path"
            }
        }
    }

    private companion object {
        const val BUCKET_NAME = "recipe-photos"
    }
}
