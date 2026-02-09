package com.plusmobileapps.chefmate.grocery.data.impl.remote

interface GroceryRemoteDataSource {
    suspend fun ensureDefaultList(ownerId: String): String

    suspend fun upsertGroceryItem(item: RemoteGroceryItem): RemoteGroceryItem

    suspend fun deleteGroceryItem(remoteId: String)

    suspend fun fetchAllGroceryItems(listId: String): List<RemoteGroceryItem>
}
