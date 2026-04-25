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
class SupabaseRecipeRemoteDataSource(private val supabaseClient: SupabaseClient) :
    RecipeRemoteDataSource {

    override suspend fun upsertRecipe(recipe: RemoteRecipe): RemoteRecipe =
        supabaseClient
            .from("recipes")
            .upsert(recipe) {
                select()
                if (recipe.id == null && recipe.clientId != null) {
                    onConflict = "client_id"
                }
            }
            .decodeSingle<RemoteRecipe>()

    override suspend fun deleteRecipe(remoteId: String) {
        supabaseClient.from("recipes").delete { filter { eq("id", remoteId) } }
    }

    override suspend fun fetchAllRecipes(ownerId: String): List<RemoteRecipe> =
        supabaseClient
            .from("recipes")
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<RemoteRecipe>()

    override suspend fun fetchAccessibleRecipes(): List<RemoteRecipe> =
        supabaseClient.from("recipes").select().decodeList<RemoteRecipe>()

    override suspend fun fetchRecipeShares(recipeId: String): List<RemoteRecipeShare> =
        supabaseClient
            .from("recipe_shares")
            .select { filter { eq("recipe_id", recipeId) } }
            .decodeList<RemoteRecipeShare>()

    override suspend fun shareRecipe(share: RemoteRecipeShare): RemoteRecipeShare =
        supabaseClient
            .from("recipe_shares")
            .insert(share) { select() }
            .decodeSingle<RemoteRecipeShare>()

    override suspend fun respondToRecipeShare(shareId: String, accept: Boolean) {
        supabaseClient.from("recipe_shares").update(
            mapOf("status" to if (accept) "accepted" else "rejected")
        ) {
            filter { eq("id", shareId) }
        }
    }

    override suspend fun removeRecipeShare(shareId: String) {
        supabaseClient.from("recipe_shares").delete { filter { eq("id", shareId) } }
    }
}
