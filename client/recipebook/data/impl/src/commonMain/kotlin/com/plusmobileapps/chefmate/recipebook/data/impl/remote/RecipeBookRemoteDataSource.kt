package com.plusmobileapps.chefmate.recipebook.data.impl.remote

interface RecipeBookRemoteDataSource {
    suspend fun upsertRecipeBook(book: RemoteRecipeBook): RemoteRecipeBook

    suspend fun deleteRecipeBook(remoteId: String)

    suspend fun fetchAllRecipeBooks(ownerId: String): List<RemoteRecipeBook>
}
