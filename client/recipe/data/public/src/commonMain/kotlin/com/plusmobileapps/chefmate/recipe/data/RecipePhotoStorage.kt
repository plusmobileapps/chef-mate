package com.plusmobileapps.chefmate.recipe.data

interface RecipePhotoStorage {
    suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String
}
