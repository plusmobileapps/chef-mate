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
}
