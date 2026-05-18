package com.plusmobileapps.chefmate.recipe.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseCategoryRemoteDataSource(private val supabaseClient: SupabaseClient) :
    CategoryRemoteDataSource {

    override suspend fun upsertCategory(category: RemoteCategory): RemoteCategory =
        supabaseClient
            .from("categories")
            .upsert(category) {
                select()
                if (category.id == null && category.clientId != null) {
                    onConflict = "client_id"
                }
            }
            .decodeSingle<RemoteCategory>()

    override suspend fun deleteCategory(remoteId: String) {
        supabaseClient.from("categories").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun fetchAllCategories(ownerId: String): List<RemoteCategory> =
        supabaseClient
            .from("categories")
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<RemoteCategory>()
}
