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

    /** Returns only the recipes belonging to the book with local id [recipeBookId]. */
    fun getRecipes(recipeBookId: Long): Flow<List<Recipe>>

    suspend fun createRecipe(recipe: Recipe): Recipe

    suspend fun updateRecipe(recipe: Recipe): Recipe

    suspend fun getRecipe(id: Long): Flow<Recipe?>

    /**
     * Looks up a locally-stored recipe by its global [remoteId], or null if none is stored. Used to
     * resolve a shared recipe link to a recipe the current user already owns or collaborates on so
     * it opens in the normal detail screen instead of the read-only public preview.
     */
    suspend fun getRecipeByRemoteId(remoteId: String): Recipe?

    /**
     * Looks up a locally-stored recipe by its device-generated [Recipe.clientId], or null if none
     * is stored. Used to resolve a recipe-to-recipe link (`chefmate://recipe/<clientId>`) to the
     * local recipe on whichever device the link is tapped.
     */
    suspend fun getRecipeByClientId(clientId: String): Recipe?

    /**
     * Fetches a public recipe by its global [remoteId] from the remote source, for a recipient
     * opening a share link to a recipe they don't have locally. The returned [Recipe] is transient
     * (not persisted; local [Recipe.id] is -1) — call [createRecipe] to save an owned copy. Fails
     * when the recipe isn't public/accessible or the fetch errors (e.g. offline).
     */
    suspend fun fetchPublicRecipe(remoteId: String): Result<Recipe>

    /**
     * Marks the recipe [id] public or private, ensuring it is pushed to the remote first (so it has
     * a [Recipe.remoteId]). Returns the recipe's remote id — the identifier a share link embeds —
     * or null if it couldn't be synced (e.g. signed out, or the push failed).
     */
    suspend fun setRecipePublic(id: Long, isPublic: Boolean): String?

    suspend fun deleteRecipe(id: Long)

    suspend fun clearLocalData()

    suspend fun syncAllUnsynced()
}
