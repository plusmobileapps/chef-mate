package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.RecipePhotoStorage

class FakeRecipePhotoStorage(var nextResult: () -> String = { "https://example.com/photo.jpg" }) :
    RecipePhotoStorage {
    val uploads = mutableListOf<Upload>()

    override suspend fun uploadPhoto(bytes: ByteArray, fileExtension: String): String {
        uploads.add(Upload(bytes = bytes, fileExtension = fileExtension))
        return nextResult()
    }

    data class Upload(val bytes: ByteArray, val fileExtension: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Upload) return false
            if (fileExtension != other.fileExtension) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }

        override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileExtension.hashCode()
    }
}
