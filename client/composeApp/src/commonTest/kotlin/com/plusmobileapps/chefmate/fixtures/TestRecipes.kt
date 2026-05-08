@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.fixtures

import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Default recipe fixtures for UI tests, ranging from a fully populated recipe down to a title-only
 * minimum. The DB schema only requires `title`; the domain model also requires `ingredients` and
 * `directions` to be non-null Strings (empty is valid).
 */
object TestRecipes {

    private val createdAt = Instant.parse("2026-01-01T00:00:00Z")

    /** Every field populated. Mirrors a real recipe a power user would save. */
    val fullyPopulated =
        Recipe(
            id = 1L,
            title = "Pasta Carbonara",
            description = "A classic Roman pasta with eggs, cheese, and cured pork.",
            ingredients =
                "200g spaghetti\n100g guanciale\n2 large eggs\n50g pecorino romano\n" +
                    "Freshly cracked black pepper\nSalt",
            directions =
                "Bring a large pot of salted water to a boil and cook pasta until al dente.\n" +
                    "Render the guanciale in a wide pan over medium heat until crisp.\n" +
                    "Whisk eggs and pecorino in a bowl with plenty of black pepper.\n" +
                    "Drain pasta and toss with guanciale, then add the egg mixture off the heat.",
            imageUrl = "https://example.test/carbonara.jpg",
            sourceUrl = "https://example.test/carbonara",
            servings = 2,
            prepTime = 10,
            cookTime = 15,
            totalTime = 25,
            calories = 620,
            starRating = 5,
            isFavorite = true,
            syncStatus = SyncStatus.SYNCED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    /** Description and image dropped; metadata still present. */
    val withoutDescriptionOrImage =
        Recipe(
            id = 2L,
            title = "Lasagna",
            description = null,
            ingredients = "Lasagna sheets\nGround beef\nTomato sauce\nMozzarella",
            directions = "Layer ingredients in a baking dish and bake at 375°F for 45 minutes.",
            imageUrl = null,
            sourceUrl = null,
            servings = 6,
            prepTime = 20,
            cookTime = 45,
            totalTime = 65,
            calories = 480,
            starRating = 4,
            isFavorite = false,
            syncStatus = SyncStatus.SYNCED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    /** Only title + ingredients + directions; no metadata, no image, no rating. */
    val ingredientsAndDirectionsOnly =
        Recipe(
            id = 3L,
            title = "Toast",
            description = null,
            ingredients = "Bread\nButter",
            directions = "Toast bread. Apply butter.",
            imageUrl = null,
            sourceUrl = null,
            servings = null,
            prepTime = null,
            cookTime = null,
            totalTime = null,
            calories = null,
            starRating = null,
            isFavorite = false,
            syncStatus = SyncStatus.NOT_SYNCED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    /** Title only — the technical minimum the DB will accept. */
    val titleOnly =
        Recipe(
            id = 4L,
            title = "Mystery Dish",
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
            syncStatus = SyncStatus.NOT_SYNCED,
            createdAt = createdAt,
            updatedAt = createdAt,
        )

    /** All four permutations from fully-populated to title-only. */
    val defaults: List<Recipe> =
        listOf(fullyPopulated, withoutDescriptionOrImage, ingredientsAndDirectionsOnly, titleOnly)
}
