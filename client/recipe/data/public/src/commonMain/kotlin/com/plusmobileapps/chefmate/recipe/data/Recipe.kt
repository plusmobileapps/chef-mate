@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.data

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class Recipe(
    val id: Long,
    val title: String,
    val description: String?,
    val ingredients: String,
    val directions: String,
    val imageUrl: String?,
    val sourceUrl: String?,
    val servings: Int?,
    val prepTime: Int?,
    val cookTime: Int?,
    val totalTime: Int?,
    val calories: Int?,
    val starRating: Int?,
    val isFavorite: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        val Empty =
            Recipe(
                id = -1,
                title = "",
                description = null,
                ingredients = "",
                directions = "",
                imageUrl = null,
                sourceUrl = null,
                servings = null,
                prepTime = null,
                cookTime = null,
                totalTime = null,
                calories = null,
                starRating = null,
                isFavorite = false,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
    }
}
