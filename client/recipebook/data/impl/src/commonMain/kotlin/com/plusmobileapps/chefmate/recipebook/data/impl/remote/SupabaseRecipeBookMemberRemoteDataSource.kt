package com.plusmobileapps.chefmate.recipebook.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseRecipeBookMemberRemoteDataSource(private val supabaseClient: SupabaseClient) :
    RecipeBookMemberRemoteDataSource {

    override suspend fun fetchMembers(bookRemoteId: String): List<RemoteRecipeBookMember> =
        supabaseClient
            .from("recipe_book_members")
            .select { filter { eq("recipe_book_id", bookRemoteId) } }
            .decodeList<RemoteRecipeBookMember>()

    override suspend fun invite(bookRemoteId: String, email: String, role: String) {
        supabaseClient
            .from("recipe_book_members")
            .insert(
                RemoteRecipeBookMember(
                    recipeBookId = bookRemoteId,
                    invitedEmail = email,
                    role = role,
                    status = "pending",
                )
            )
    }

    override suspend fun deleteMember(memberId: String) {
        supabaseClient.from("recipe_book_members").delete { filter { eq("id", memberId) } }
    }

    override suspend fun fetchPendingInvites(email: String): List<RemoteRecipeBookInvite> {
        // Two plain queries instead of a `recipe_books!inner(name)` embed: the inner-join embed
        // silently dropped rows even when both the member row and the book were readable to the
        // invitee under RLS. invited_email is stored lowercased and callers pass a lowercased
        // address, so the exact match is correct and safe (no LIKE wildcards).
        val members =
            supabaseClient
                .from("recipe_book_members")
                .select(Columns.raw("id, recipe_book_id, user_id, invited_email, role, status")) {
                    filter {
                        eq("invited_email", email)
                        eq("status", "pending")
                    }
                }
                .decodeList<RemoteRecipeBookMember>()
        if (members.isEmpty()) return emptyList()

        val bookNamesById =
            supabaseClient
                .from("recipe_books")
                .select(Columns.raw("id, name")) {
                    filter { isIn("id", members.map { it.recipeBookId }.distinct()) }
                }
                .decodeList<RemoteRecipeBookName>()
                .associate { it.id to it.name }

        return members.mapNotNull { member ->
            val id = member.id ?: return@mapNotNull null
            RemoteRecipeBookInvite(
                id = id,
                recipeBookId = member.recipeBookId,
                role = member.role,
                status = member.status,
                book = RemoteInviteBook(name = bookNamesById[member.recipeBookId].orEmpty()),
            )
        }
    }

    override suspend fun acceptInvite(memberId: String, userId: String) {
        supabaseClient.from("recipe_book_members").update(
            JsonObject(
                mapOf("user_id" to JsonPrimitive(userId), "status" to JsonPrimitive("accepted"))
            )
        ) {
            filter { eq("id", memberId) }
        }
    }
}
