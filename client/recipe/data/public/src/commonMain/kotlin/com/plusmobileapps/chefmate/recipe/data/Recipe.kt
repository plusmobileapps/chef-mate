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

        /** Populated sample recipe for use in @Preview composables and screenshot tests. */
        val Sample =
            Recipe(
                id = 1L,
                title = "Pasta Carbonara",
                description = "A classic Roman pasta with eggs, cheese, and cured pork.",
                ingredients =
                    "200g spaghetti\n100g guanciale\n2 large eggs\n50g pecorino romano\n" +
                        "Freshly cracked black pepper\nSalt",
                directions =
                    "Bring a large pot of salted water to a boil and cook pasta until al dente.\n" +
                        "Meanwhile, render the guanciale in a wide pan over medium heat until crisp.\n" +
                        "Whisk eggs and pecorino in a bowl with plenty of black pepper.\n" +
                        "Drain pasta (reserving a cup of water) and add to the pan off the heat.\n" +
                        "Pour in the egg mixture and toss vigorously, loosening with pasta water until silky.",
                imageUrl = null,
                sourceUrl = null,
                servings = 2,
                prepTime = 10,
                cookTime = 15,
                totalTime = 25,
                calories = 620,
                starRating = 5,
                isFavorite = true,
                syncStatus = SyncStatus.SYNCED,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )
    }
}

enum class SyncStatus {
    NOT_SYNCED,
    SYNCING,
    SYNCED,
}
