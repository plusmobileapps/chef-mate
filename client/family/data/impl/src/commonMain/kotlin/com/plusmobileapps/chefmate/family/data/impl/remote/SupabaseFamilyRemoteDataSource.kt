package com.plusmobileapps.chefmate.family.data.impl.remote

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.family.data.remote.FamilyRemoteDataSource
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamily
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyCollaborator
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyInvite
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyMember
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseFamilyRemoteDataSource(private val supabaseClient: SupabaseClient) :
    FamilyRemoteDataSource {

    override fun observeChanges(): Flow<Unit> {
        val channel = supabaseClient.channel(REALTIME_CHANNEL)
        // Register a Postgres-change binding per table before subscribing — the SDK requires
        // bindings to exist when the channel joins. Row-level security scopes each stream to rows
        // this user can see, so we simply re-reconcile on any emission.
        val tableChanges = REALTIME_TABLES.map { table ->
            channel.postgresChangeFlow<PostgresAction>(schema = "public") { this.table = table }
        }
        return merge(*tableChanges.toTypedArray())
            .map {}
            .onStart { channel.subscribe() }
            .onCompletion {
                // Runs on cancellation too (e.g. sign-out). Force the leave through even though the
                // collecting coroutine is being cancelled, so the server-side channel is released
                // before a same-named channel is recreated on the next sign-in.
                withContext(NonCancellable) { supabaseClient.realtime.removeChannel(channel) }
            }
    }

    override suspend fun fetchCurrentFamily(): RemoteFamily? =
        supabaseClient.postgrest.rpc("current_family").decodeList<RemoteFamily>().firstOrNull()

    override suspend fun fetchMembers(familyRemoteId: String): List<RemoteFamilyCollaborator> =
        supabaseClient.postgrest
            .rpc(
                "family_members_with_profiles",
                buildJsonObject { put("p_family_id", familyRemoteId) },
            )
            .decodeList<RemoteFamilyCollaborator>()

    override suspend fun fetchPendingInvites(): List<RemoteFamilyInvite> =
        supabaseClient.postgrest.rpc("family_pending_invites").decodeList<RemoteFamilyInvite>()

    override suspend fun createFamily(name: String, ownerId: String): RemoteFamily =
        supabaseClient
            .from("families")
            .insert(RemoteFamily(name = name, ownerId = ownerId)) { select() }
            .decodeSingle<RemoteFamily>()

    override suspend fun renameFamily(familyRemoteId: String, name: String) {
        supabaseClient.from("families").update(JsonObject(mapOf("name" to JsonPrimitive(name)))) {
            filter { eq("id", familyRemoteId) }
        }
    }

    override suspend fun deleteFamily(familyRemoteId: String) {
        supabaseClient.from("families").delete { filter { eq("id", familyRemoteId) } }
    }

    override suspend fun invite(familyRemoteId: String, email: String, invitedBy: String) {
        supabaseClient
            .from("family_members")
            .insert(
                RemoteFamilyMember(
                    familyId = familyRemoteId,
                    invitedEmail = email,
                    invitedBy = invitedBy,
                    role = "member",
                    status = "pending",
                )
            )
    }

    override suspend fun deleteMember(memberId: String) {
        supabaseClient.from("family_members").delete { filter { eq("id", memberId) } }
    }

    override suspend fun leaveFamily(familyRemoteId: String, userId: String) {
        supabaseClient.from("family_members").delete {
            filter {
                eq("family_id", familyRemoteId)
                eq("user_id", userId)
            }
        }
    }

    override suspend fun acceptInvite(memberId: String, userId: String) {
        supabaseClient.from("family_members").update(
            JsonObject(
                mapOf("user_id" to JsonPrimitive(userId), "status" to JsonPrimitive("accepted"))
            )
        ) {
            filter { eq("id", memberId) }
        }
    }

    override suspend fun rejectInvite(memberId: String) {
        supabaseClient.from("family_members").update(
            JsonObject(mapOf("status" to JsonPrimitive("rejected")))
        ) {
            filter { eq("id", memberId) }
        }
    }

    private companion object {
        const val REALTIME_CHANNEL = "family-sync"
        val REALTIME_TABLES = listOf("families", "family_members")
    }
}
