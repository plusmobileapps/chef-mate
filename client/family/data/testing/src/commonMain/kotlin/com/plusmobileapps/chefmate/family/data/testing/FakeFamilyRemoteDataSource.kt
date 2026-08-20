package com.plusmobileapps.chefmate.family.data.testing

import com.plusmobileapps.chefmate.family.data.remote.FamilyRemoteDataSource
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamily
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyCollaborator
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyInvite
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * In-memory [FamilyRemoteDataSource] standing in for Supabase. Mutations update the backing state
 * the way the real RPCs would, so a repository test can drive create → invite → accept end to end.
 */
class FakeFamilyRemoteDataSource : FamilyRemoteDataSource {

    /** The family the *caller* currently belongs to, as `current_family()` would report it. */
    var currentFamily: RemoteFamily? = null

    var members: MutableList<RemoteFamilyCollaborator> = mutableListOf()

    var pendingInvites: MutableList<RemoteFamilyInvite> = mutableListOf()

    /** Set to have every call throw, exercising the repository's failure handling. */
    var errorToThrow: Exception? = null

    val invitedEmails: MutableList<String> = mutableListOf()
    val deletedMemberIds: MutableList<String> = mutableListOf()
    val rejectedMemberIds: MutableList<String> = mutableListOf()
    var deletedFamilyId: String? = null
        private set

    var leftFamilyId: String? = null
        private set

    private val changes =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 16,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /** Emits a realtime change so the repository re-reconciles. */
    suspend fun emitChange() {
        changes.emit(Unit)
    }

    override fun observeChanges(): Flow<Unit> = changes

    override suspend fun fetchCurrentFamily(): RemoteFamily? {
        throwIfConfigured()
        return currentFamily
    }

    override suspend fun fetchMembers(familyRemoteId: String): List<RemoteFamilyCollaborator> {
        throwIfConfigured()
        return members.toList()
    }

    override suspend fun fetchPendingInvites(): List<RemoteFamilyInvite> {
        throwIfConfigured()
        return pendingInvites.toList()
    }

    override suspend fun createFamily(name: String, ownerId: String): RemoteFamily {
        throwIfConfigured()
        val created = RemoteFamily(id = "family-remote-1", name = name, ownerId = ownerId)
        currentFamily = created
        members =
            mutableListOf(
                RemoteFamilyCollaborator(
                    memberId = null,
                    email = "owner@example.com",
                    name = null,
                    role = "owner",
                    status = "accepted",
                    isOwner = true,
                )
            )
        return created
    }

    override suspend fun renameFamily(familyRemoteId: String, name: String) {
        throwIfConfigured()
        currentFamily = currentFamily?.copy(name = name)
    }

    override suspend fun deleteFamily(familyRemoteId: String) {
        throwIfConfigured()
        deletedFamilyId = familyRemoteId
        currentFamily = null
        members.clear()
    }

    override suspend fun invite(familyRemoteId: String, email: String, invitedBy: String) {
        throwIfConfigured()
        invitedEmails += email
        members +=
            RemoteFamilyCollaborator(
                memberId = "member-${members.size + 1}",
                email = email,
                name = null,
                role = "member",
                status = "pending",
                isOwner = false,
            )
    }

    override suspend fun deleteMember(memberId: String) {
        throwIfConfigured()
        deletedMemberIds += memberId
        members.removeAll { it.memberId == memberId }
    }

    override suspend fun leaveFamily(familyRemoteId: String, userId: String) {
        throwIfConfigured()
        leftFamilyId = familyRemoteId
        currentFamily = null
        members.clear()
    }

    override suspend fun acceptInvite(memberId: String, userId: String) {
        throwIfConfigured()
        val invite = pendingInvites.firstOrNull { it.memberId == memberId } ?: return
        pendingInvites.removeAll { it.memberId == memberId }
        currentFamily =
            RemoteFamily(id = invite.familyId, name = invite.familyName, ownerId = "other-owner")
        members =
            mutableListOf(
                RemoteFamilyCollaborator(
                    memberId = null,
                    email = "owner@example.com",
                    name = null,
                    role = "owner",
                    status = "accepted",
                    isOwner = true,
                ),
                RemoteFamilyCollaborator(
                    memberId = memberId,
                    email = "invitee@example.com",
                    name = null,
                    role = "member",
                    status = "accepted",
                    isOwner = false,
                ),
            )
    }

    override suspend fun rejectInvite(memberId: String) {
        throwIfConfigured()
        rejectedMemberIds += memberId
        pendingInvites.removeAll { it.memberId == memberId }
    }

    private fun throwIfConfigured() {
        errorToThrow?.let { throw it }
    }
}
