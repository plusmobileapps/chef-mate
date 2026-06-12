package com.plusmobileapps.chefmate.grocery.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.grocery.data.remote.GroceryRemoteDataSource
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryItem
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryList
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryListCollaborator
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryListInvite
import com.plusmobileapps.chefmate.grocery.data.remote.RemoteGroceryListMember
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseGroceryRemoteDataSource(private val supabaseClient: SupabaseClient) :
    GroceryRemoteDataSource {

    override suspend fun ensureDefaultList(ownerId: String): String {
        val existing =
            supabaseClient
                .from("grocery_lists")
                .select {
                    filter { eq("owner_id", ownerId) }
                    limit(1)
                }
                .decodeList<RemoteGroceryList>()

        if (existing.isNotEmpty()) {
            return existing.first().id!!
        }

        val created =
            supabaseClient
                .from("grocery_lists")
                .insert(RemoteGroceryList(ownerId = ownerId)) { select() }
                .decodeSingle<RemoteGroceryList>()

        return created.id!!
    }

    override suspend fun upsertGroceryItem(item: RemoteGroceryItem): RemoteGroceryItem =
        supabaseClient
            .from("grocery_items")
            .upsert(item) {
                select()
                if (item.id == null && item.clientId != null) {
                    onConflict = "client_id"
                }
            }
            .decodeSingle<RemoteGroceryItem>()

    override suspend fun deleteGroceryItem(remoteId: String) {
        supabaseClient.from("grocery_items").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun fetchAllGroceryItems(listId: String): List<RemoteGroceryItem> =
        supabaseClient
            .from("grocery_items")
            .select { filter { eq("list_id", listId) } }
            .decodeList<RemoteGroceryItem>()

    override suspend fun createGroceryList(list: RemoteGroceryList): RemoteGroceryList =
        supabaseClient
            .from("grocery_lists")
            .insert(list) { select() }
            .decodeSingle<RemoteGroceryList>()

    override suspend fun fetchGroceryLists(ownerId: String): List<RemoteGroceryList> =
        supabaseClient
            .from("grocery_lists")
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<RemoteGroceryList>()

    override suspend fun deleteGroceryList(remoteId: String) {
        supabaseClient.from("grocery_lists").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun updateGroceryList(list: RemoteGroceryList): RemoteGroceryList =
        supabaseClient
            .from("grocery_lists")
            .update(list) {
                select()
                filter { eq("id", list.id!!) }
            }
            .decodeSingle<RemoteGroceryList>()

    override suspend fun fetchAccessibleGroceryLists(): List<RemoteGroceryList> =
        supabaseClient.from("grocery_lists").select().decodeList<RemoteGroceryList>()

    override suspend fun fetchListMembers(listId: String): List<RemoteGroceryListMember> =
        supabaseClient
            .from("grocery_list_members")
            .select { filter { eq("list_id", listId) } }
            .decodeList<RemoteGroceryListMember>()

    override suspend fun fetchListCollaborators(
        listId: String
    ): List<RemoteGroceryListCollaborator> =
        supabaseClient.postgrest
            .rpc("grocery_list_collaborators", buildJsonObject { put("p_list_id", listId) })
            .decodeList<RemoteGroceryListCollaborator>()

    override suspend fun inviteToList(member: RemoteGroceryListMember): RemoteGroceryListMember =
        supabaseClient
            .from("grocery_list_members")
            .insert(member) { select() }
            .decodeSingle<RemoteGroceryListMember>()

    override suspend fun fetchPendingInvitations(email: String): List<RemoteGroceryListInvite> =
        supabaseClient.postgrest
            .rpc("grocery_list_pending_invites")
            .decodeList<RemoteGroceryListInvite>()

    override suspend fun respondToInvitation(memberId: String, userId: String, accept: Boolean) {
        val payload =
            if (accept) {
                JsonObject(
                    mapOf("user_id" to JsonPrimitive(userId), "status" to JsonPrimitive("accepted"))
                )
            } else {
                JsonObject(mapOf("status" to JsonPrimitive("rejected")))
            }
        supabaseClient.from("grocery_list_members").update(payload) {
            filter { eq("id", memberId) }
        }
    }

    override suspend fun removeFromList(memberId: String) {
        supabaseClient.from("grocery_list_members").delete { filter { eq("id", memberId) } }
    }
}
