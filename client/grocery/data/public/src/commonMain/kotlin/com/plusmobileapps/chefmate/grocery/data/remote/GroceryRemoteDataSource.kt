package com.plusmobileapps.chefmate.grocery.data.remote

interface GroceryRemoteDataSource {
    suspend fun ensureDefaultList(ownerId: String): String

    suspend fun upsertGroceryItem(item: RemoteGroceryItem): RemoteGroceryItem

    suspend fun deleteGroceryItem(remoteId: String)

    suspend fun fetchAllGroceryItems(listId: String): List<RemoteGroceryItem>

    suspend fun createGroceryList(list: RemoteGroceryList): RemoteGroceryList

    suspend fun fetchGroceryLists(ownerId: String): List<RemoteGroceryList>

    suspend fun fetchAccessibleGroceryLists(): List<RemoteGroceryList>

    suspend fun deleteGroceryList(remoteId: String)

    suspend fun updateGroceryList(list: RemoteGroceryList): RemoteGroceryList

    suspend fun fetchListMembers(listId: String): List<RemoteGroceryListMember>

    suspend fun inviteToList(member: RemoteGroceryListMember): RemoteGroceryListMember

    suspend fun respondToInvitation(memberId: String, accept: Boolean)

    suspend fun removeFromList(memberId: String)
}
