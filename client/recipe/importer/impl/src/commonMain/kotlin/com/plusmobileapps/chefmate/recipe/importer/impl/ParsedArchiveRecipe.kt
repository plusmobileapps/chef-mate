package com.plusmobileapps.chefmate.recipe.importer.impl

/** A recipe extracted from an import archive, with its image bytes resolved from the zip. */
internal class ParsedArchiveRecipe(
    val title: String,
    val description: String?,
    val ingredients: List<String>,
    val directions: List<String>,
    val sourceUrl: String?,
    val servings: Int?,
    val prepTime: Int?,
    val cookTime: Int?,
    val totalTime: Int?,
    val calories: Int?,
    val starRating: Int?,
    val imageBytes: ByteArray?,
    val imageExtension: String?,
) {
    val hasImage: Boolean
        get() = imageBytes != null
}
