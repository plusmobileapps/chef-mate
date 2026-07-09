package com.plusmobileapps.chefmate.grocery.data.impl.remote

interface GroceryAutocompleteRemoteDataSource {
    suspend fun upsertItem(item: RemoteGroceryAutocompleteItem): RemoteGroceryAutocompleteItem

    suspend fun deleteItem(remoteId: String)

    suspend fun fetchAllItems(ownerId: String): List<RemoteGroceryAutocompleteItem>
}
