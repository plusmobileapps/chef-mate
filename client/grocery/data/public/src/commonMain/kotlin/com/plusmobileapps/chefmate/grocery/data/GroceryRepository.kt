@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.grocery.data

import kotlin.time.ExperimentalTime
import kotlinx.coroutines.flow.Flow

interface GroceryRepository {
    fun getGroceries(): Flow<List<GroceryItem>>

    fun getGroceries(listId: Long): Flow<List<GroceryItem>>

    fun getGroceryLists(): Flow<List<GroceryListModel>>

    suspend fun addGrocery(name: String)

    suspend fun addGrocery(listId: Long, name: String)

    suspend fun addGroceries(names: List<String>)

    suspend fun addGroceries(listId: Long, names: List<String>)

    suspend fun addGroceries(listId: Long, names: List<String>, recipeName: String?)

    suspend fun updateChecked(item: GroceryItem, isChecked: Boolean)

    suspend fun deleteGrocery(item: GroceryItem)

    suspend fun getGrocery(id: Long): GroceryItem?

    suspend fun updateGrocery(item: GroceryItem)

    suspend fun syncAllUnsynced()

    suspend fun createGroceryList(name: String): Long

    suspend fun deleteGroceryList(id: Long)

    suspend fun renameGroceryList(id: Long, name: String)

    suspend fun ensureDefaultList(): Long

    suspend fun deleteAllGroceries(listId: Long)

    suspend fun deletePurchasedGroceries(listId: Long)

    suspend fun clearLocalData()

    fun getListCollaborators(listId: Long): Flow<List<ListCollaborator>>

    /**
     * Pulls the latest members for [listId] from the remote and replaces the local cache that
     * [getListCollaborators] observes. Call when opening a collaborator view so members appear even
     * when no full sync has run this session (e.g. right after sign-in). No-op when
     * offline/unsynced.
     */
    suspend fun refreshListMembers(listId: Long)

    suspend fun inviteCollaborator(listId: Long, email: String, role: ListRole = ListRole.EDITOR)

    suspend fun removeCollaborator(listId: Long, collaboratorId: Long)

    suspend fun acceptInvitation(memberId: String)

    suspend fun rejectInvitation(memberId: String)

    fun getPendingInvitations(): Flow<List<GroceryListInvite>>
}
