package com.plusmobileapps.chefmate.recipe.data.impl.remote

interface RecipeRemoteDataSource {
    suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe

    suspend fun deleteRecipe(remoteId: String)

    suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe>
}
