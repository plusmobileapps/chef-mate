package com.plusmobileapps.chefmate.recipebook.data.impl.remote

interface RecipeBookRemoteDataSource {
    suspend fun upsertRecipeBook(book: RemoteRecipeBook): RemoteRecipeBook

    suspend fun deleteRecipeBook(remoteId: String)

    /**
     * Every recipe book the current user can access — owned plus those shared with them. Row-level
     * security on the server scopes the result, so no owner filter is applied here.
     */
    suspend fun fetchAccessibleRecipeBooks(): List<RemoteRecipeBook>
}
