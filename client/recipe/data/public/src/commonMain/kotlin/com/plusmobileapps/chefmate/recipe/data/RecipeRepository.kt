package com.plusmobileapps.chefmate.recipe.data

import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    suspend fun createRecipe(recipe: Recipe): Recipe

    suspend fun updateRecipe(recipe: Recipe): Recipe

    suspend fun getRecipe(id: Long): Flow<Recipe?>

    suspend fun deleteRecipe(id: Long)

    suspend fun clearLocalData()

    suspend fun syncAllUnsynced()

    fun getSharedRecipes(): Flow<List<Recipe>>

    suspend fun shareRecipe(recipeId: Long, email: String, role: RecipeRole = RecipeRole.VIEWER)

    suspend fun forkRecipe(recipeId: Long): Recipe

    suspend fun acceptRecipeShare(recipeId: Long)

    suspend fun rejectRecipeShare(recipeId: Long)

    fun getRecipeCollaborators(recipeId: Long): Flow<List<RecipeCollaborator>>
}
