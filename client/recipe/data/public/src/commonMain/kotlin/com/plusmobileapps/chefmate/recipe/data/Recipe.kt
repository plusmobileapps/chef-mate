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
    val categories: Set<Category> = emptySet(),
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
                categories = emptySet(),
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
                categories =
                    setOf(
                        Category(id = 1L, name = "Dinner", builtinId = BuiltinCategory.DINNER.id)
                    ),
                syncStatus = SyncStatus.SYNCED,
                createdAt = Instant.DISTANT_PAST,
                updatedAt = Instant.DISTANT_PAST,
            )

        /**
         * Sample recipe whose ingredients are split into sub-sections via the [IngredientSection]
         * header convention — for previews/tests of the grouped layout.
         */
        val SampleWithSections =
            Sample.copy(
                id = 7L,
                title = "Peruvian Chicken with Green Sauce",
                description = "Marinated chicken thighs with a creamy jalapeño cilantro sauce.",
                ingredients =
                    "For the Peruvian chicken:\n1½ lbs boneless skinless chicken thighs\n" +
                        "½ cup soy sauce\n¾ cup cilantro, roughly chopped\n6 garlic cloves\n" +
                        "1 tsp ground cumin\n" +
                        "For the green sauce:\n½ cup sour cream\n1 jalapeño, deseeded\n" +
                        "2 garlic cloves\n1 Tbsp lime juice\nSalt, to taste",
            )

        /** Variants of [Sample] for seeding the local DB in FAKE environment / dev contexts. */
        val Samples: List<Recipe> =
            listOf(
                Sample,
                Sample.copy(
                    id = 2L,
                    title = "Margherita Pizza",
                    description = "Neapolitan-style pizza with tomato, mozzarella, and basil.",
                    servings = 4,
                    prepTime = 90,
                    cookTime = 5,
                    totalTime = 95,
                    calories = 720,
                    starRating = 4,
                    isFavorite = false,
                ),
                Sample.copy(
                    id = 3L,
                    title = "Beef Tacos",
                    description = "Quick weeknight tacos with seasoned ground beef.",
                    servings = 4,
                    prepTime = 10,
                    cookTime = 15,
                    totalTime = 25,
                    calories = 540,
                    starRating = 4,
                    isFavorite = true,
                ),
                Sample.copy(
                    id = 4L,
                    title = "Caesar Salad",
                    description = "Crisp romaine, parmesan, and a creamy anchovy dressing.",
                    servings = 2,
                    prepTime = 15,
                    cookTime = 0,
                    totalTime = 15,
                    calories = 380,
                    starRating = 3,
                    isFavorite = false,
                ),
                Sample.copy(
                    id = 5L,
                    title = "Chocolate Chip Cookies",
                    description = "Chewy cookies with pools of dark chocolate.",
                    servings = 24,
                    prepTime = 20,
                    cookTime = 12,
                    totalTime = 32,
                    calories = 180,
                    starRating = 5,
                    isFavorite = true,
                ),
                Sample.copy(
                    id = 6L,
                    title = "Chicken Curry",
                    description = "Aromatic curry with coconut milk and warm spices.",
                    servings = 4,
                    prepTime = 15,
                    cookTime = 35,
                    totalTime = 50,
                    calories = 610,
                    starRating = 4,
                    isFavorite = false,
                ),
            )
    }
}

enum class SyncStatus {
    NOT_SYNCED,
    SYNCING,
    SYNCED,
}
