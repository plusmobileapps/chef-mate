package com.plusmobileapps.chefmate.recipe.data

/**
 * Extracts a recipe from a single image — a photo of a cookbook page, food label, screenshot, or
 * handwritten card — into [ExtractedRecipeData].
 *
 * Implemented by the Gemini-backed extractor so both the AI-chat photo-attach flow and the
 * standalone "Scan from photo" flow can reuse one vision call. Lives in the public layer (rather
 * than `aichat/impl`) so the recipe flow can inject it without depending on another `impl` module.
 */
interface RecipeImageExtractor {
    suspend fun extractFromImage(bytes: ByteArray, mimeType: String): ExtractedRecipeData
}

/**
 * Thrown when image extraction fails. [message] is a stable code (`MISSING_API_KEY`,
 * `REQUEST_FAILED`, `EMPTY_RESPONSE`, `MALFORMED_JSON`, `INCOMPLETE_RECIPE`) that callers map to a
 * user-facing error.
 */
class RecipeExtractionException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)
