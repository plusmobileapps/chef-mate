package com.plusmobileapps.chefmate.recipebook.data.impl.remote

interface RecipeBookMemberRemoteDataSource {
    /** Members (accepted + pending) of the book with remote id [bookRemoteId]. */
    suspend fun fetchMembers(bookRemoteId: String): List<RemoteRecipeBookMember>

    /** Inserts a pending invite for [email] at [role] on [bookRemoteId]. */
    suspend fun invite(bookRemoteId: String, email: String, role: String)

    /** Deletes the member/invite row [memberId]. */
    suspend fun deleteMember(memberId: String)

    /** Pending invites addressed to [email], joined with each book's name. */
    suspend fun fetchPendingInvites(email: String): List<RemoteRecipeBookInvite>

    /** Accepts invite [memberId]: stamps [userId] and flips status to accepted. */
    suspend fun acceptInvite(memberId: String, userId: String)
}
