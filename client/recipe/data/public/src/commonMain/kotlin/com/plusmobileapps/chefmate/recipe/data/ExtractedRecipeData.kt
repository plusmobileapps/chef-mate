package com.plusmobileapps.chefmate.recipe.data

import kotlinx.serialization.Serializable

@Serializable
data class ExtractedRecipeData(
    val title: String,
    val description: String?,
    val ingredients: List<String>,
    val directions: List<String>,
    val imageUrl: String?,
    val sourceUrl: String,
    val servings: Int?,
    val prepTime: Int?,
    val cookTime: Int?,
    val totalTime: Int?,
    val calories: Int?,
)
