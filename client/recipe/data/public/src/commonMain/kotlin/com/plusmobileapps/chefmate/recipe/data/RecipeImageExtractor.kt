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

/** The distinct ways recipe extraction (from chat text or an image) can fail. */
enum class RecipeExtractionError {
    /** No Gemini API key is configured. */
    MISSING_API_KEY,
    /** The network request to Gemini failed. */
    REQUEST_FAILED,
    /** Gemini returned no usable candidate/content. */
    EMPTY_RESPONSE,
    /** Gemini's structured-output payload couldn't be parsed as a recipe. */
    MALFORMED_JSON,
    /** A recipe was parsed but is missing required fields (title, ingredients, or directions). */
    INCOMPLETE_RECIPE,
}

/**
 * Thrown when recipe extraction fails. [error] is a typed [RecipeExtractionError] so callers can
 * branch exhaustively (e.g. surface a "missing API key" message) without matching on strings.
 */
class RecipeExtractionException(val error: RecipeExtractionError, cause: Throwable? = null) :
    RuntimeException(error.name, cause)
