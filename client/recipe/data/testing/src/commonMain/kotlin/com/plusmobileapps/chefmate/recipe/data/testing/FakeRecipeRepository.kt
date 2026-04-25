package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeCollaborator
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.data.RecipeRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRecipeRepository(
    private val recipes: MutableStateFlow<List<Recipe>> = MutableStateFlow(emptyList())
) : RecipeRepository {
    override fun getRecipes(): Flow<List<Recipe>> = recipes.asStateFlow()

    override suspend fun createRecipe(recipe: Recipe): Recipe {
        recipes.value = recipes.value + recipe
        return recipe
    }

    override suspend fun updateRecipe(recipe: Recipe): Recipe {
        recipes.value = recipes.value.map { if (it.id == recipe.id) recipe else it }
        return recipe
    }

    override suspend fun getRecipe(id: Long): Flow<Recipe?> =
        recipes.value.firstOrNull { it.id == id }?.let { MutableStateFlow(it) }
            ?: MutableStateFlow(null)

    override suspend fun deleteRecipe(id: Long) {
        recipes.value = recipes.value.filterNot { it.id == id }
    }

    override suspend fun clearLocalData() {
        recipes.value = emptyList()
    }

    override suspend fun syncAllUnsynced() {}

    override fun getSharedRecipes(): Flow<List<Recipe>> = MutableStateFlow(emptyList())

    override suspend fun shareRecipe(recipeId: Long, email: String, role: RecipeRole) {}

    override suspend fun forkRecipe(recipeId: Long): Recipe =
        recipes.value.first { it.id == recipeId }.copy(id = recipeId + 1000)

    override suspend fun acceptRecipeShare(recipeId: Long) {}

    override suspend fun rejectRecipeShare(recipeId: Long) {}

    override fun getRecipeCollaborators(recipeId: Long): Flow<List<RecipeCollaborator>> =
        MutableStateFlow(emptyList())
}
