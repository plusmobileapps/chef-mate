package com.plusmobileapps.chefmate.recipe.data.impl.remote

interface CategoryRemoteDataSource {
    suspend fun upsertCategory(category: RemoteCategory): RemoteCategory

    suspend fun deleteCategory(remoteId: String)

    suspend fun fetchAllCategories(ownerId: String): List<RemoteCategory>
}
