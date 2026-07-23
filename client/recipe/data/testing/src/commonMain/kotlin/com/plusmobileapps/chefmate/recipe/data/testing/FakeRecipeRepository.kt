package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeRecipeRepository(
    private val recipes: MutableStateFlow<List<Recipe>> = MutableStateFlow(emptyList())
) : RecipeRepository {

    /** Public recipes reachable by remote id via [fetchPublicRecipe], keyed by remote id. */
    val publicRecipes: MutableMap<String, Recipe> = mutableMapOf()

    /**
     * Remote id returned by [setRecipePublic]; null simulates an unauthenticated/failed publish.
     */
    var setPublicResult: String? = "remote-id"

    /** Records the last [setRecipePublic] invocation for assertions. */
    var lastSetPublic: Pair<Long, Boolean>? = null

    override fun getRecipes(): Flow<List<Recipe>> = recipes.asStateFlow()

    override fun getRecipes(presets: Set<BuiltinCategory>?): Flow<List<Recipe>> =
        if (presets.isNullOrEmpty()) {
            getRecipes()
        } else {
            getRecipes().map { list -> list.filter { it.matchesFilter(presets) } }
        }

    override fun getRecipes(recipeBookId: Long): Flow<List<Recipe>> =
        getRecipes().map { list -> list.filter { recipeBookId in it.recipeBookIds } }

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

    override suspend fun getRecipeByRemoteId(remoteId: String): Recipe? =
        recipes.value.firstOrNull { it.remoteId == remoteId }

    override suspend fun getRecipeByClientId(clientId: String): Recipe? =
        recipes.value.firstOrNull { it.clientId == clientId }

    override suspend fun fetchPublicRecipe(remoteId: String): Result<Recipe> =
        publicRecipes[remoteId]?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("No public recipe with id $remoteId"))

    override suspend fun setRecipePublic(id: Long, isPublic: Boolean): String? {
        lastSetPublic = id to isPublic
        recipes.value = recipes.value.map { if (it.id == id) it.copy(isPublic = isPublic) else it }
        return setPublicResult
    }

    override suspend fun deleteRecipe(id: Long) {
        recipes.value = recipes.value.filterNot { it.id == id }
    }

    override suspend fun deleteLocalRecipesInBook(recipeBookId: Long) {
        recipes.value =
            recipes.value.mapNotNull { recipe ->
                when {
                    recipeBookId !in recipe.recipeBookIds -> recipe
                    recipe.recipeBookIds.size <= 1 -> null
                    else -> recipe.copy(recipeBookIds = recipe.recipeBookIds - recipeBookId)
                }
            }
    }

    override suspend fun clearLocalData() {
        recipes.value = emptyList()
    }

    override suspend fun syncAllUnsynced() {}

    private fun Recipe.matchesFilter(presets: Set<BuiltinCategory>): Boolean {
        val recipeBuiltins = categories.mapNotNull { BuiltinCategory.fromId(it.builtinId) }.toSet()
        if (recipeBuiltins.isEmpty()) return BuiltinCategory.OTHER in presets
        return recipeBuiltins.any { it in presets }
    }
}
