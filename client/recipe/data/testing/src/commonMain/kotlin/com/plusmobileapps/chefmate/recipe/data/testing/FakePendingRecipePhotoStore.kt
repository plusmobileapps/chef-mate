package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.PendingPhoto
import com.plusmobileapps.chefmate.recipe.data.PendingRecipePhotoStore

/**
 * In-memory [PendingRecipePhotoStore] for tests. Mirrors the production single-slot behaviour;
 * [consume] returns and clears the stored photo. [puts] records every [put] for assertions.
 */
class FakePendingRecipePhotoStore : PendingRecipePhotoStore {

    val puts = mutableListOf<PendingPhoto>()
    private var slot: PendingPhoto? = null

    override fun put(bytes: ByteArray, fileExtension: String) {
        val photo = PendingPhoto(bytes = bytes, fileExtension = fileExtension)
        slot = photo
        puts.add(photo)
    }

    override fun consume(): PendingPhoto? {
        val current = slot
        slot = null
        return current
    }
}
