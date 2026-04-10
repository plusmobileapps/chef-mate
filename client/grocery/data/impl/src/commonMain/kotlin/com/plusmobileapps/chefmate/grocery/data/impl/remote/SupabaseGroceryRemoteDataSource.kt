package com.plusmobileapps.chefmate.grocery.data.impl.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
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
}
