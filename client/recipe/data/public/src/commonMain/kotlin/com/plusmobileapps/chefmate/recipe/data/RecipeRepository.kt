package com.plusmobileapps.chefmate.recipe.data

import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun createRecipe(recipe: Recipe): Recipe

    suspend fun updateRecipe(recipe: Recipe): Recipe

    suspend fun getRecipe(id: Long): Flow<Recipe?>

    suspend fun deleteRecipe(id: Long)
}