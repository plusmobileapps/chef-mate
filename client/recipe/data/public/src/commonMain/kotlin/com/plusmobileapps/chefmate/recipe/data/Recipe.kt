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
    val syncStatus: SyncStatus = SyncStatus.NOT_SYNCED,
    val createdAt: Instant,
    val updatedAt: Instant,
    val forkedFromRemoteId: String? = null,
    val forkedFromTitle: String? = null,
    val role: RecipeRole = RecipeRole.OWNER,
    val isShared: Boolean = false,
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
                forkedFromRemoteId = null,
                forkedFromTitle = null,
                role = RecipeRole.OWNER,
                isShared = false,
            )
    }
}

enum class SyncStatus {
    NOT_SYNCED,
    SYNCING,
    SYNCED,
}
