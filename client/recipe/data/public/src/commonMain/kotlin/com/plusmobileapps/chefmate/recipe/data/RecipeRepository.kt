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
     * Fetches the recipes [profileId] has published to their public profile, newest first. Like
     * [fetchPublicRecipe] the results are transient previews (local [Recipe.id] is -1) — call
     * [createRecipe] to save an owned copy.
     *
     * Only published recipes come back, never merely-shared ones: a recipe made public for a share
     * link is deliberately unlisted, so it must not appear on anyone's profile.
     */
    suspend fun fetchPublishedRecipes(
        profileId: String,
        limit: Int = 50,
        offset: Int = 0,
    ): Result<List<Recipe>>

    /**
     * Marks the recipe [id] public or private, ensuring it is pushed to the remote first (so it has
     * a [Recipe.remoteId]). Returns the recipe's remote id — the identifier a share link embeds —
     * or null if it couldn't be synced (e.g. signed out, or the push failed).
     */
    suspend fun setRecipePublic(id: Long, isPublic: Boolean): String?

    /**
     * Lists the recipe [id] on the owner's public profile, or removes it. Publishing implies
     * [setRecipePublic] — a listed recipe must be readable — so this pushes both flags;
     * unpublishing leaves [Recipe.isPublic] alone so any share link already handed out keeps
     * working.
     *
     * Returns the recipe's remote id, or null if it couldn't be synced (e.g. signed out).
     */
    suspend fun setRecipePublished(id: Long, published: Boolean): String?

    /**
     * Files every recipe in [recipeIds] under the book with local id [bookId], in a single
     * transaction. Book membership is additive and many-to-many — a recipe already in [bookId] is
     * left untouched, and its existing book memberships are preserved. Each affected recipe is
     * marked dirty so the new attachment syncs to the remote. No-op when [recipeIds] is empty.
     */
    suspend fun addRecipesToBook(recipeIds: Set<Long>, bookId: Long)

    /**
     * Attaches [category] to every recipe in [recipeIds], in a single transaction. Additive and
     * idempotent — a recipe already tagged with [category] is left untouched and its other
     * categories are preserved. Each affected recipe is marked dirty so the new attachment syncs to
     * the remote. Callers pass a materialized [Category] (see
     * [CategoryRepository.materializeBuiltin]). No-op when [recipeIds] is empty.
     */
    suspend fun addRecipesToCategory(recipeIds: Set<Long>, category: Category)

    suspend fun deleteRecipe(id: Long)

    /**
     * Drops the recipes that live *only* in the book with local id [recipeBookId] from the local
     * cache, and detaches the rest from that book. Nothing is deleted remotely — this is for
     * leaving a shared book, where the recipes belong to the book's owner and must survive on the
     * server. Recipes also filed under another book are kept locally.
     */
    suspend fun deleteLocalRecipesInBook(recipeBookId: Long)

    suspend fun clearLocalData()

    suspend fun syncAllUnsynced()
}
