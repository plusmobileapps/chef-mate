package com.plusmobileapps.chefmate.grocery.data.testing

import com.plusmobileapps.chefmate.grocery.data.remote.GroceryRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryList
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FakeGroceryRemoteDataSource : GroceryRemoteDataSource {
    val remoteLists = mutableMapOf<String, MutableList<RemoteGroceryList>>()
    val remoteItems = mutableMapOf<String, MutableList<RemoteGroceryItem>>()
    private var nextListId = 1

    override suspend fun ensureDefaultList(ownerId: String): String {
        val lists = remoteLists.getOrPut(ownerId) { mutableListOf() }
        if (lists.isNotEmpty()) return lists.first().id!!
        val newList =
            RemoteGroceryList(
                id = "remote-default-${nextListId++}",
                name = "My Grocery List",
                ownerId = ownerId,
            )
        lists.add(newList)
        return newList.id!!
    }

    override suspend fun upsertGroceryItem(item: RemoteGroceryItem): RemoteGroceryItem {
        val items = remoteItems.getOrPut(item.listId) { mutableListOf() }
        val existing = items.indexOfFirst { it.id == item.id || it.clientId == item.clientId }
        val result = item.copy(id = item.id ?: "remote-item-${Uuid.random()}")
        if (existing >= 0) {
            items[existing] = result
        } else {
            items.add(result)
        }
        return result
    }

    override suspend fun deleteGroceryItem(remoteId: String) {
        remoteItems.values.forEach { it.removeAll { item -> item.id == remoteId } }
    }

    override suspend fun fetchAllGroceryItems(listId: String): List<RemoteGroceryItem> =
        remoteItems[listId].orEmpty()

    override suspend fun createGroceryList(list: RemoteGroceryList): RemoteGroceryList {
        val result = list.copy(id = list.id ?: "remote-list-${nextListId++}")
        val ownerLists = remoteLists.getOrPut(list.ownerId) { mutableListOf() }
        ownerLists.add(result)
        return result
    }

    override suspend fun fetchGroceryLists(ownerId: String): List<RemoteGroceryList> =
        remoteLists[ownerId].orEmpty()

    override suspend fun deleteGroceryList(remoteId: String) {
        remoteLists.values.forEach { it.removeAll { list -> list.id == remoteId } }
    }

    override suspend fun updateGroceryList(list: RemoteGroceryList): RemoteGroceryList {
        val ownerLists = remoteLists[list.ownerId] ?: return list
        val index = ownerLists.indexOfFirst { it.id == list.id }
        if (index >= 0) ownerLists[index] = list
        return list
    }
}
