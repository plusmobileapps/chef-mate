package com.plusmobileapps.chefmate.recipebook.data.impl.remote

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
        // DIAGNOSTIC: the RLS SELECT policy matches on auth.jwt() ->> 'email'; log it next to the
        // filter so a mismatch (vs. the user-object email) is visible.
        Logger.i(tag = "RecipeBookMemberRDS") {
            "fetchPendingInvites(v2): filterEmail='$email' jwtEmail='${currentJwtEmail()}'"
        }
        val authCtx =
            try {
                supabaseClient.postgrest.rpc("debug_auth_context").data
            } catch (t: Throwable) {
                "rpc error: ${t.message}"
            }
        Logger.i(tag = "RecipeBookMemberRDS") { "server auth context = $authCtx" }
        // Don't filter on invited_email server-side: that's a case-sensitive `eq`, and a row stored
        // with different casing (e.g. an autocapitalised address from before the
        // lowercase-on-invite
        // fix) would pass the case-insensitive RLS check yet be excluded by the filter. RLS already
        // scopes this to the caller's own invites; match the address case-insensitively on-device.
        val raw =
            supabaseClient
                .from("recipe_book_members")
                .select(Columns.raw("id, recipe_book_id, user_id, invited_email, role, status")) {
                    filter { eq("status", "pending") }
                }
                .decodeList<RemoteRecipeBookMember>()
        val members = raw.filter { it.invitedEmail.trim().lowercase() == email }
        Logger.i(tag = "RecipeBookMemberRDS") {
            "fetchPendingInvites(v2): raw pending rows = ${raw.size}, mine after email match = ${members.size}"
        }
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

    /** Decodes the `email` claim from the current access-token JWT (the value RLS matches on). */
    @OptIn(ExperimentalEncodingApi::class)
    private fun currentJwtEmail(): String? =
        try {
            val token = supabaseClient.auth.currentSessionOrNull()?.accessToken
            val payload = token?.split(".")?.getOrNull(1)
            if (payload == null) {
                null
            } else {
                val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
                val json = Base64.UrlSafe.decode(padded).decodeToString()
                Json.parseToJsonElement(json).jsonObject["email"]?.jsonPrimitive?.contentOrNull
            }
        } catch (t: Throwable) {
            Logger.w(throwable = t, tag = "RecipeBookMemberRDS") { "couldn't decode jwt email" }
            null
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
