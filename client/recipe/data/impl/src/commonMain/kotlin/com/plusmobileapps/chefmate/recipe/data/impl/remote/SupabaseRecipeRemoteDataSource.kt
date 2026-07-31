package com.plusmobileapps.chefmate.recipe.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

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

    override suspend fun fetchAccessibleRecipes(): List<RemoteRecipe> =
        // Deliberately NOT a blanket `from("recipes").select()`: the permissive "Anyone can view
        // public recipes" RLS policy would OR every is_public row into the result, so a fresh
        // account would sync down the entire public catalog (#487). The get_accessible_recipes()
        // RPC returns exactly owned + shared-book recipes.
        supabaseClient.postgrest.rpc("get_accessible_recipes").decodeList<RemoteRecipe>()

    override suspend fun fetchPublicRecipe(remoteId: String): RemoteRecipe? =
        supabaseClient
            .from("recipes")
            .select {
                filter {
                    eq("id", remoteId)
                    eq("is_public", true)
                }
            }
            .decodeSingleOrNull<RemoteRecipe>()

    override suspend fun fetchPublishedRecipes(
        profileId: String,
        limit: Int,
        offset: Int,
    ): List<RemoteRecipe> =
        supabaseClient.postgrest
            .rpc(
                "get_published_recipes",
                buildJsonObject {
                    put("p_profile_id", JsonPrimitive(profileId))
                    put("p_limit", JsonPrimitive(limit))
                    put("p_offset", JsonPrimitive(offset))
                },
            )
            .decodeList<RemoteRecipe>()

    override suspend fun setRecipeCategories(
        recipeRemoteId: String,
        categoryRemoteIds: Set<String>,
    ) {
        // Replace-all: drop all existing rows for the recipe, then insert the desired set. Two
        // round-trips is fine for the small attachment counts typical per recipe; the PK
        // (recipe_id, category_id) keeps the insert safe under retry.
        supabaseClient.from("recipe_categories").delete {
            filter { eq("recipe_id", recipeRemoteId) }
        }
        if (categoryRemoteIds.isEmpty()) return
        supabaseClient.from("recipe_categories").upsert(
            categoryRemoteIds.map { categoryId ->
                RemoteRecipeCategory(recipeId = recipeRemoteId, categoryId = categoryId)
            }
        ) {
            onConflict = "recipe_id,category_id"
        }
    }

    override suspend fun fetchRecipeCategoryAttachments(): Map<String, Set<String>> {
        // RLS scopes rows to recipes the user can access (own + shared).
        val rows =
            supabaseClient
                .from("recipe_categories")
                .select(Columns.raw("recipe_id, category_id"))
                .decodeList<RemoteRecipeCategory>()
        return rows.groupBy { it.recipeId }.mapValues { (_, g) -> g.map { it.categoryId }.toSet() }
    }

    override suspend fun setRecipeBooks(recipeRemoteId: String, bookRemoteIds: Set<String>) {
        // Replace-all, mirroring setRecipeCategories: drop existing rows for the recipe, then
        // insert
        // the desired set. The PK (recipe_book_id, recipe_id) keeps the insert safe under retry.
        supabaseClient.from("recipe_book_recipes").delete {
            filter { eq("recipe_id", recipeRemoteId) }
        }
        if (bookRemoteIds.isEmpty()) return
        supabaseClient.from("recipe_book_recipes").upsert(
            bookRemoteIds.map { bookId ->
                RemoteRecipeBookRecipe(recipeId = recipeRemoteId, recipeBookId = bookId)
            }
        ) {
            onConflict = "recipe_book_id,recipe_id"
        }
    }

    override suspend fun fetchRecipeBookAttachments(): Map<String, Set<String>> {
        // RLS scopes rows to books the user can access (own + shared).
        val rows =
            supabaseClient
                .from("recipe_book_recipes")
                .select(Columns.raw("recipe_id, recipe_book_id"))
                .decodeList<RemoteRecipeBookRecipe>()
        return rows
            .groupBy { it.recipeId }
            .mapValues { (_, g) -> g.map { it.recipeBookId }.toSet() }
    }
}
