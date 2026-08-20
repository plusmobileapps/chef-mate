package com.plusmobileapps.chefmate.family.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The signed-in user's family and its membership.
 *
 * Reads ([family], [members], [pendingInvites]) are served from a local cache so the Family screen
 * renders offline. Writes are **online operations** performed remote-first and throw on
 * network/permission failure, because membership can only be arbitrated by the server — notably the
 * "one family per user" rule, which the database enforces with a partial unique index. Callers
 * surface the failure rather than queueing it.
 */
interface FamilyRepository {
    /**
     * The user's family, or null when they aren't in one (or are signed out). Later phases read
     * this to decide whether a grocery list, recipe book, or meal can be shared with the family.
     */
    val family: StateFlow<Family?>

    /**
     * Everyone on [family] — owner first, then accepted members, then invites. Empty with no
     * family.
     */
    fun members(): Flow<List<FamilyMember>>

    /** Family invites addressed to the current user's email and awaiting a response. */
    fun pendingInvites(): Flow<List<FamilyInvite>>

    /**
     * Creates a family named [name] with the current user as owner.
     *
     * @throws AlreadyInFamilyException if the user already belongs to one.
     */
    suspend fun createFamily(name: String)

    /** Renames the family. Owner only. */
    suspend fun renameFamily(name: String)

    /**
     * Invites [email] to the family. The invitee gets an email (sent by a database trigger) and an
     * in-app notification; the invite stays pending until they accept. Owner only.
     */
    suspend fun invite(email: String)

    /** Removes the member / cancels the invite with remote [memberId]. Owner only. */
    suspend fun removeMember(memberId: String)

    /**
     * Removes the current user's own membership, then drops the family from the local cache. Owners
     * can't leave their own family; they call [deleteFamily] instead.
     */
    suspend fun leaveFamily()

    /** Deletes the family for everyone. Owner only. */
    suspend fun deleteFamily()

    /**
     * Accepts the invite with remote [memberId] and pulls the family.
     *
     * @throws AlreadyInFamilyException if the user is already in a family — they have to leave it
     *   first, since a user can only belong to one.
     */
    suspend fun acceptInvite(memberId: String)

    /** Declines the invite with remote [memberId]. */
    suspend fun declineInvite(memberId: String)

    /** Re-pulls the family, its members, and pending invites from the server. */
    suspend fun refresh()

    /** Drops all cached family state. Called on sign-out. */
    suspend fun clearLocalData()
}

/**
 * Thrown when an operation would put the user in a second family. A user can belong to at most one;
 * the database rejects the write and the UI asks them to leave their current family first.
 */
class AlreadyInFamilyException(message: String = "Already in a family") : Exception(message)
