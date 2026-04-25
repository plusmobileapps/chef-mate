package com.plusmobileapps.chefmate.recipe.data.impl.remote

interface RecipeRemoteDataSource {
    suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe

    suspend fun deleteRecipe(remoteId: String)

    suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe>

    /**
     * Replaces the recipe's attached-category set in the `recipe_categories` join table: deletes
     * any rows for [recipeRemoteId] whose `category_id` isn't in [categoryRemoteIds], then inserts
     * the missing rows. Idempotent — safe to call repeatedly with the same set.
     */
    suspend fun setRecipeCategories(recipeRemoteId: String, categoryRemoteIds: Set<String>)

    /**
     * Returns the full set of recipe ↔ category attachments owned by [ownerId], keyed by recipe
     * remote ID. Recipes with no attachments are omitted (callers should treat absence as empty).
     */
    suspend fun fetchRecipeCategoryAttachments(ownerId: String): Map<String, Set<String>>

    suspend fun fetchAccessibleRecipes(): List<RemoteRecipe>

    suspend fun fetchRecipeShares(recipeId: String): List<RemoteRecipeShare>

    suspend fun shareRecipe(share: RemoteRecipeShare): RemoteRecipeShare

    suspend fun respondToRecipeShare(shareId: String, accept: Boolean)

    suspend fun removeRecipeShare(shareId: String)
}
