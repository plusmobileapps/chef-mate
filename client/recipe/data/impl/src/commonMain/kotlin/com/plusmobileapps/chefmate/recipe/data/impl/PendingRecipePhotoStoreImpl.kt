package com.plusmobileapps.chefmate.recipe.data.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.recipe.data.PendingPhoto
import com.plusmobileapps.chefmate.recipe.data.PendingRecipePhotoStore
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.concurrent.Volatile

/**
 * Single-slot in-memory implementation of [PendingRecipePhotoStore]. App-scoped so the photo picked
 * by the producing flow survives navigation to the Edit Recipe screen, where it's consumed exactly
 * once. [Volatile] guards the cross-thread hand-off (producer may put from an IO dispatcher; the
 * editor view model consumes on main).
 */
@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PendingRecipePhotoStoreImpl : PendingRecipePhotoStore {

    @Volatile private var slot: PendingPhoto? = null

    override fun put(bytes: ByteArray, fileExtension: String) {
        slot = PendingPhoto(bytes = bytes, fileExtension = fileExtension)
    }

    override fun consume(): PendingPhoto? {
        val current = slot
        slot = null
        return current
    }
}
