package com.plusmobileapps.chefmate.recipe.data

interface RecipePhotoStorage {
    suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String

    /**
     * Best-effort delete of a previously uploaded photo. URLs that don't point at the recipe-photos
     * bucket are ignored; failures are swallowed so caller flows aren't disrupted.
     */
    suspend fun deletePhoto(publicUrl: String)
}
