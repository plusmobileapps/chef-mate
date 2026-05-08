@file:OptIn(ExperimentalUuidApi::class)

package com.plusmobileapps.chefmate.recipe.data.impl.remote

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
        val ownerFolder =
            (authRepository.state.value as? AuthState.Authenticated)?.user?.userId
                ?: ANONYMOUS_FOLDER
        val bucket = supabaseClient.storage.from(BUCKET_NAME)
        val sanitizedExtension = fileExtension.trimStart('.').lowercase().ifBlank { "jpg" }
        val path = "$ownerFolder/${Uuid.random()}.$sanitizedExtension"
        bucket.upload(path = path, data = bytes) { upsert = false }
        return bucket.publicUrl(path)
    }

    private companion object {
        const val BUCKET_NAME = "recipe-photos"
        const val ANONYMOUS_FOLDER = "anonymous"
    }
}
