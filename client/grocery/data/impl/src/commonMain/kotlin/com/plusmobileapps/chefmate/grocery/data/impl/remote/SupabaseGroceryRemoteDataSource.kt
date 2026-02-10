package com.plusmobileapps.chefmate.grocery.data.impl.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(
    scope = AppScope::class,
    boundType = GroceryRemoteDataSource::class,
)
class SupabaseGroceryRemoteDataSource(
    private val supabaseClient: SupabaseClient,
) : GroceryRemoteDataSource {

    override suspend fun ensureDefaultList(ownerId: String): String {
        val existing = supabaseClient
            .from("grocery_lists")
            .select {
                filter { eq("owner_id", ownerId) }
                limit(1)
            }
            .decodeList<RemoteGroceryList>()

        if (existing.isNotEmpty()) {
            return existing.first().id!!
        }

        val created = supabaseClient
            .from("grocery_lists")
            .insert(RemoteGroceryList(ownerId = ownerId)) {
                select()
            }
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
        supabaseClient
            .from("grocery_items")
            .delete {
                filter { eq("id", remoteId) }
            }
    }

    override suspend fun fetchAllGroceryItems(listId: String): List<RemoteGroceryItem> =
        supabaseClient
            .from("grocery_items")
            .select {
                filter { eq("list_id", listId) }
            }
            .decodeList<RemoteGroceryItem>()

    override suspend fun createGroceryList(list: RemoteGroceryList): RemoteGroceryList =
        supabaseClient
            .from("grocery_lists")
            .insert(list) {
                select()
            }
            .decodeSingle<RemoteGroceryList>()

    override suspend fun fetchGroceryLists(ownerId: String): List<RemoteGroceryList> =
        supabaseClient
            .from("grocery_lists")
            .select {
                filter { eq("owner_id", ownerId) }
            }
            .decodeList<RemoteGroceryList>()

    override suspend fun deleteGroceryList(remoteId: String) {
        supabaseClient
            .from("grocery_lists")
            .delete {
                filter { eq("id", remoteId) }
            }
    }

    override suspend fun updateGroceryList(list: RemoteGroceryList): RemoteGroceryList =
        supabaseClient
            .from("grocery_lists")
            .update(list) {
                select()
                filter { eq("id", list.id!!) }
            }
            .decodeSingle<RemoteGroceryList>()
}
