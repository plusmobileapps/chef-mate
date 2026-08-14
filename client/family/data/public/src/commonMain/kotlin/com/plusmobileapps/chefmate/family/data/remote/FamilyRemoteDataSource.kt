package com.plusmobileapps.chefmate.family.data.remote

import kotlinx.coroutines.flow.Flow

/** Supabase access for families and their membership. Every method throws on failure. */
interface FamilyRemoteDataSource {
    /**
     * Emits when a family or membership row the caller can see changes, so the repository can
     * re-reconcile. Auto-reconnects are the caller's responsibility.
     */
    fun observeChanges(): Flow<Unit>

    /**
     * The caller's family, or null when they aren't in one.
     *
     * Goes through the `current_family()` RPC rather than selecting from `families`: RLS
     * deliberately lets a pending invitee read the family row they were invited to, so a plain
     * select would pull an unjoined family into the local cache.
     */
    suspend fun fetchCurrentFamily(): RemoteFamily?

    /** Everyone on [familyRemoteId], including the synthesized owner entry. */
    suspend fun fetchMembers(familyRemoteId: String): List<RemoteFamilyCollaborator>

    /** Pending family invites addressed to the caller's email. */
    suspend fun fetchPendingInvites(): List<RemoteFamilyInvite>

    /** Creates a family owned by [ownerId] and returns the stored row. */
    suspend fun createFamily(name: String, ownerId: String): RemoteFamily

    /** Renames [familyRemoteId]. */
    suspend fun renameFamily(familyRemoteId: String, name: String)

    /** Deletes [familyRemoteId] for everyone. */
    suspend fun deleteFamily(familyRemoteId: String)

    /** Invites [email] to [familyRemoteId] as a member, attributed to [invitedBy]. */
    suspend fun invite(familyRemoteId: String, email: String, invitedBy: String)

    /**
     * Deletes the member row [memberId] — removing a member, cancelling, or declining an invite.
     */
    suspend fun deleteMember(memberId: String)

    /** Deletes the caller's own membership of [familyRemoteId]. */
    suspend fun leaveFamily(familyRemoteId: String, userId: String)

    /** Marks the invite [memberId] accepted and links it to [userId]. */
    suspend fun acceptInvite(memberId: String, userId: String)

    /** Marks the invite [memberId] rejected, keeping the row so the owner sees the outcome. */
    suspend fun rejectInvite(memberId: String)
}
