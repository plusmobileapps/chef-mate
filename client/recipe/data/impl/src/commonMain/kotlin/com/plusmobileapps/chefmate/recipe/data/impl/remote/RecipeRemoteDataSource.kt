package com.plusmobileapps.chefmate.recipe.data.impl.remote

interface RecipeRemoteDataSource {
    suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe

    suspend fun deleteRecipe(remoteId: String)

    suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe>

    suspend fun fetchAccessibleRecipes(): List<RemoteRecipe>

    suspend fun fetchRecipeShares(recipeId: String): List<RemoteRecipeShare>

    suspend fun shareRecipe(share: RemoteRecipeShare): RemoteRecipeShare

    suspend fun respondToRecipeShare(shareId: String, accept: Boolean)

    suspend fun removeRecipeShare(shareId: String)
}
