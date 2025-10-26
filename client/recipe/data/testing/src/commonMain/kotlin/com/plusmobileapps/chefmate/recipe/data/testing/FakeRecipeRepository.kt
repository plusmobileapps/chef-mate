package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRecipeRepository(
    private val recipes: MutableStateFlow<List<Recipe>> = MutableStateFlow(emptyList()),
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
}
