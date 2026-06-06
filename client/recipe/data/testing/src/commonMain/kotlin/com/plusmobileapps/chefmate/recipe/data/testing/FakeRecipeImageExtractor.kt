package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionError
import com.plusmobileapps.chefmate.recipe.data.RecipeExtractionException
import com.plusmobileapps.chefmate.recipe.data.RecipeImageExtractor

/**
 * Test [RecipeImageExtractor]. Returns [response] (or throws [error] if set) and records every call
 * for assertions.
 */
class FakeRecipeImageExtractor(var response: ExtractedRecipeData? = null) : RecipeImageExtractor {

    val calls = mutableListOf<Call>()
    var error: RecipeExtractionException? = null

    override suspend fun extractFromImage(bytes: ByteArray, mimeType: String): ExtractedRecipeData {
        calls.add(Call(bytes = bytes, mimeType = mimeType))
        error?.let { throw it }
        return response ?: throw RecipeExtractionException(RecipeExtractionError.EMPTY_RESPONSE)
    }

    data class Call(val bytes: ByteArray, val mimeType: String) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Call) return false
            return mimeType == other.mimeType && bytes.contentEquals(other.bytes)
        }

        override fun hashCode(): Int = 31 * bytes.contentHashCode() + mimeType.hashCode()
    }
}
