package com.plusmobileapps.chefmate.recipebook.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseRecipeBookRemoteDataSource(private val supabaseClient: SupabaseClient) :
    RecipeBookRemoteDataSource {

    override suspend fun upsertRecipeBook(book: RemoteRecipeBook): RemoteRecipeBook =
        supabaseClient
            .from("recipe_books")
            .upsert(book) {
                select()
                if (book.id == null && book.clientId != null) {
                    onConflict = "client_id"
                }
            }
            .decodeSingle<RemoteRecipeBook>()

    override suspend fun deleteRecipeBook(remoteId: String) {
        supabaseClient.from("recipe_books").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun fetchAccessibleRecipeBooks(): List<RemoteRecipeBook> =
        supabaseClient.from("recipe_books").select().decodeList<RemoteRecipeBook>()
}
