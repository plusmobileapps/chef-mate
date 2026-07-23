package com.plusmobileapps.chefmate.grocery.data.testing

import com.plusmobileapps.chefmate.grocery.data.remote.GroceryRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryList
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryListCollaborator
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryListInvite
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryListMember
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@OptIn(ExperimentalUuidApi::class)
class FakeGroceryRemoteDataSource : GroceryRemoteDataSource {
    val remoteLists = mutableMapOf<String, MutableList<RemoteGroceryList>>()
    val remoteItems = mutableMapOf<String, MutableList<RemoteGroceryItem>>()
    private var nextListId = 1

    /** Emit here to simulate a realtime change arriving from another device. */
    val changes = MutableSharedFlow<Unit>(extraBufferCapacity = 16)

    override fun observeChanges(): Flow<Unit> = changes

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
        addOwnerMember(newList)
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

    override suspend fun updateGroceryItem(item: RemoteGroceryItem): RemoteGroceryItem {
        remoteItems.values.forEach { items ->
            val index = items.indexOfFirst { it.id == item.id }
            if (index >= 0) items[index] = item
        }
        return item
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
        addOwnerMember(result)
        return result
    }

    /**
     * Mirrors the production DB trigger that auto-inserts an owner row into `grocery_list_members`
     * whenever a list is created. Without this, `syncListMembers` would never observe the owner
     * membership that revealed the "own list shows up under Shared with you" bug.
     */
    private fun addOwnerMember(list: RemoteGroceryList) {
        val id = list.id ?: return
        remoteMembers
            .getOrPut(id) { mutableListOf() }
            .add(
                RemoteGroceryListMember(
                    id = "remote-owner-${Uuid.random()}",
                    listId = id,
                    userId = list.ownerId,
                    invitedEmail = "",
                    role = "owner",
                    status = "accepted",
                )
            )
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

    val remoteMembers = mutableMapOf<String, MutableList<RemoteGroceryListMember>>()

    override suspend fun fetchAccessibleGroceryLists(): List<RemoteGroceryList> =
        remoteLists.values.flatten()

    override suspend fun fetchListMembers(listId: String): List<RemoteGroceryListMember> =
        remoteMembers[listId].orEmpty()

    override suspend fun fetchListCollaborators(
        listId: String
    ): List<RemoteGroceryListCollaborator> =
        remoteMembers[listId].orEmpty().map { member ->
            RemoteGroceryListCollaborator(
                memberId = member.id,
                email = member.invitedEmail,
                name = null,
                role = member.role,
                status = member.status,
                isOwner = member.role == "owner",
            )
        }

    override suspend fun inviteToList(member: RemoteGroceryListMember): RemoteGroceryListMember {
        val result = member.copy(id = member.id ?: "remote-member-${Uuid.random()}")
        remoteMembers.getOrPut(member.listId) { mutableListOf() }.add(result)
        return result
    }

    override suspend fun fetchPendingInvitations(email: String): List<RemoteGroceryListInvite> =
        remoteMembers.values
            .flatten()
            .filter {
                it.status == "pending" && it.invitedEmail.trim().lowercase() == email.lowercase()
            }
            .mapNotNull { member ->
                val id = member.id ?: return@mapNotNull null
                val list =
                    remoteLists.values.flatten().firstOrNull { remoteList ->
                        remoteList.id == member.listId
                    }
                RemoteGroceryListInvite(
                    id = id,
                    listId = member.listId,
                    listName = list?.name.orEmpty(),
                    role = member.role,
                    status = member.status,
                )
            }

    override suspend fun respondToInvitation(memberId: String, userId: String, accept: Boolean) {
        remoteMembers.values.forEach { members ->
            val idx = members.indexOfFirst { it.id == memberId }
            if (idx >= 0) {
                members[idx] =
                    members[idx].copy(
                        userId = if (accept) userId else members[idx].userId,
                        status = if (accept) "accepted" else "rejected",
                    )
            }
        }
    }

    override suspend fun removeFromList(memberId: String) {
        remoteMembers.values.forEach { it.removeAll { m -> m.id == memberId } }
    }
}
