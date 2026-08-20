package com.plusmobileapps.chefmate.recipe.data.testing

import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import kotlin.time.Instant
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

    /** Recipes returned by [fetchPublishedRecipes], keyed by the owner's profile id. */
    val publishedRecipes: MutableMap<String, List<Recipe>> = mutableMapOf()

    /** Set to make [fetchPublishedRecipes] fail, for exercising the offline state. */
    var fetchPublishedFailure: Throwable? = null

    /** Records the last [setRecipePublished] invocation for assertions. */
    var lastSetPublished: Pair<Long, Boolean>? = null

    /** Remote id returned by [setRecipePublished]; null simulates a signed-out publish. */
    var setPublishedResult: String? = "remote-id"

    /** A fixed timestamp so published recipes are deterministic in tests. */
    var publishedAt: Instant = Instant.DISTANT_PAST

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

    override suspend fun fetchPublishedRecipes(
        profileId: String,
        limit: Int,
        offset: Int,
    ): Result<List<Recipe>> =
        fetchPublishedFailure?.let { Result.failure(it) }
            ?: Result.success(publishedRecipes[profileId].orEmpty().drop(offset).take(limit))

    override suspend fun setRecipePublished(id: Long, published: Boolean): String? {
        lastSetPublished = id to published
        recipes.value =
            recipes.value.map {
                when {
                    it.id != id -> it
                    // Mirrors the real repository: publishing forces isPublic on, unpublishing
                    // leaves it alone so an existing share link keeps working.
                    published -> it.copy(publishedAt = publishedAt, isPublic = true)
                    else -> it.copy(publishedAt = null)
                }
            }
        return setPublishedResult
    }

    override suspend fun addRecipesToBook(recipeIds: Set<Long>, bookId: Long) {
        recipes.value =
            recipes.value.map {
                if (it.id in recipeIds) it.copy(recipeBookIds = it.recipeBookIds + bookId) else it
            }
    }

    override suspend fun addRecipesToCategory(recipeIds: Set<Long>, category: Category) {
        recipes.value =
            recipes.value.map {
                if (it.id in recipeIds) it.copy(categories = it.categories + category) else it
            }
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
