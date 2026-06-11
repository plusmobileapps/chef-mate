package com.plusmobileapps.chefmate.recipe.data.impl.remote

interface RecipeRemoteDataSource {
    suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe

    suspend fun deleteRecipe(remoteId: String)

    /**
     * Every recipe the current user can access — owned plus those in books shared with them.
     * Row-level security scopes the result, so no owner filter is applied.
     */
    suspend fun fetchAccessibleRecipes(): List<RemoteRecipe>

    /**
     * Replaces the recipe's attached-category set in the `recipe_categories` join table: deletes
     * any rows for [recipeRemoteId] whose `category_id` isn't in [categoryRemoteIds], then inserts
     * the missing rows. Idempotent — safe to call repeatedly with the same set.
     */
    suspend fun setRecipeCategories(recipeRemoteId: String, categoryRemoteIds: Set<String>)

    /**
     * Returns every accessible recipe ↔ category attachment (RLS-scoped), keyed by recipe remote
     * ID. Recipes with no attachments are omitted (callers should treat absence as empty).
     */
    suspend fun fetchRecipeCategoryAttachments(): Map<String, Set<String>>

    /**
     * Replaces the recipe's book membership in the `recipe_book_recipes` join table: deletes any
     * rows for [recipeRemoteId] whose `recipe_book_id` isn't in [bookRemoteIds], then inserts the
     * missing rows. Idempotent — safe to call repeatedly with the same set.
     */
    suspend fun setRecipeBooks(recipeRemoteId: String, bookRemoteIds: Set<String>)

    /**
     * Returns every accessible recipe ↔ book membership (RLS-scoped), keyed by recipe remote ID.
     * Recipes with no memberships are omitted (callers should treat absence as empty).
     */
    suspend fun fetchRecipeBookAttachments(): Map<String, Set<String>>
}
