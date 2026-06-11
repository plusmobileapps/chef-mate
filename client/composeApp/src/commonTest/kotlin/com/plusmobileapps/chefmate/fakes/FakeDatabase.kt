package com.plusmobileapps.chefmate.fakes

import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.recipe.data.Recipe

class FakeDatabase(private val delegate: Database = provideTestDatabase()) : Database by delegate {

    fun addRecipe(recipe: Recipe) {
        recipeQueries.create(
            title = recipe.title,
            description = recipe.description,
            ingredients = recipe.ingredients.takeIf { it.isNotEmpty() },
            directions = recipe.directions.takeIf { it.isNotEmpty() },
            imageUrl = recipe.imageUrl,
            sourceUrl = recipe.sourceUrl,
            servings = recipe.servings?.toLong(),
            prepTime = recipe.prepTime?.toLong(),
            cookTime = recipe.cookTime?.toLong(),
            totalTime = recipe.totalTime?.toLong(),
            calories = recipe.calories?.toLong(),
            starRating = recipe.starRating?.toLong(),
            isFavorite = recipe.isFavorite,
            createdAt = recipe.createdAt.toString(),
            updatedAt = recipe.updatedAt.toString(),
            clientId = null,
            ownerId = null,
        )
        val recipeId =
            recipeQueries.lastInsertId().executeAsOne().MAX ?: error("Failed to get last insert id")
        for (category in recipe.categories) {
            recipeCategoryQueries.attach(recipeId = recipeId, categoryId = category.id)
        }
        // Every recipe belongs to a book. Production attaches one via createRecipe / the migration;
        // this fixture inserts rows directly, so it files them under a seeded default book — the
        // same book RecipeBookRepository resolves on startup — so the book-scoped list shows them.
        val bookIds = recipe.recipeBookIds.ifEmpty { setOf(ensureDefaultBookId()) }
        for (bookId in bookIds) {
            recipeBookRecipeQueries.attach(recipeBookId = bookId, recipeId = recipeId)
        }
    }

    fun addRecipes(recipes: Iterable<Recipe>) {
        recipes.forEach(::addRecipe)
    }

    fun addRecipes(vararg recipes: Recipe) {
        recipes.forEach(::addRecipe)
    }

    fun clearRecipes() {
        recipeQueries.deleteAll()
    }

    /** Creates a non-default recipe book and returns its local id. */
    fun createBook(name: String): Long {
        recipeBookQueries.create(
            name = name,
            isDefault = false,
            createdAt = DEFAULT_TIMESTAMP,
            updatedAt = DEFAULT_TIMESTAMP,
            clientId = null,
            ownerId = null,
        )
        return recipeBookQueries.lastInsertId().executeAsOne().MAX
            ?: error("Failed to get last insert id")
    }

    private fun ensureDefaultBookId(): Long {
        recipeBookQueries.getDefault().executeAsOneOrNull()?.let {
            return it.id
        }
        recipeBookQueries.create(
            name = "My Recipes",
            isDefault = true,
            createdAt = DEFAULT_TIMESTAMP,
            updatedAt = DEFAULT_TIMESTAMP,
            clientId = null,
            ownerId = null,
        )
        return recipeBookQueries.lastInsertId().executeAsOne().MAX
            ?: error("Failed to get last insert id")
    }

    private companion object {
        const val DEFAULT_TIMESTAMP = "2024-01-01T00:00:00Z"
    }
}
