package com.plusmobileapps.chefmate.recipe.data

import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getRecipes(): Flow<List<Recipe>>

    /**
     * Returns recipes that have at least one attached [Category] whose `builtinId` matches one of
     * [presets]. Recipes with no categories at all match only when [BuiltinCategory.OTHER] is in
     * [presets] — preserving the legacy "uncategorized = Other" rule. An empty or null filter set
     * returns every recipe (same as [getRecipes]).
     *
     * Filtering by user-created categories will land alongside the list filter sheet update.
     */
    fun getRecipes(presets: Set<BuiltinCategory>?): Flow<List<Recipe>>

    suspend fun createRecipe(recipe: Recipe): Recipe

    suspend fun updateRecipe(recipe: Recipe): Recipe

    suspend fun getRecipe(id: Long): Flow<Recipe?>

    suspend fun deleteRecipe(id: Long)

    suspend fun clearLocalData()

    suspend fun syncAllUnsynced()
}
