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

    /**
     * Replaces the recipe's book membership in the `recipe_book_recipes` join table: deletes any
     * rows for [recipeRemoteId] whose `recipe_book_id` isn't in [bookRemoteIds], then inserts the
     * missing rows. Idempotent — safe to call repeatedly with the same set.
     */
    suspend fun setRecipeBooks(recipeRemoteId: String, bookRemoteIds: Set<String>)

    /**
     * Returns the full set of recipe ↔ book memberships owned by [ownerId], keyed by recipe remote
     * ID. Recipes with no memberships are omitted (callers should treat absence as empty).
     */
    suspend fun fetchRecipeBookAttachments(ownerId: String): Map<String, Set<String>>
}
