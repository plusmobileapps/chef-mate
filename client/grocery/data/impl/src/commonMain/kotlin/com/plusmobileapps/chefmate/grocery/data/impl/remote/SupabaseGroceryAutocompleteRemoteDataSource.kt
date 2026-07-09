package com.plusmobileapps.chefmate.grocery.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseGroceryAutocompleteRemoteDataSource(private val supabaseClient: SupabaseClient) :
    GroceryAutocompleteRemoteDataSource {

    override suspend fun upsertItem(
        item: RemoteGroceryAutocompleteItem
    ): RemoteGroceryAutocompleteItem =
        supabaseClient
            .from("grocery_autocomplete_items")
            .upsert(item) {
                select()
                if (item.id == null && item.clientId != null) {
                    onConflict = "client_id"
                }
            }
            .decodeSingle<RemoteGroceryAutocompleteItem>()

    override suspend fun deleteItem(remoteId: String) {
        supabaseClient.from("grocery_autocomplete_items").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun fetchAllItems(ownerId: String): List<RemoteGroceryAutocompleteItem> =
        supabaseClient
            .from("grocery_autocomplete_items")
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<RemoteGroceryAutocompleteItem>()
}
