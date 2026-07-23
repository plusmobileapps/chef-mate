package com.plusmobileapps.chefmate.recipebook.data

/**
 * Manages recipe-book collaboration: listing/inviting/removing collaborators and accepting or
 * declining the invites addressed to the current user. These are online operations backed by
 * Supabase; methods throw on network/permission failure so callers can surface an error.
 */
interface RecipeBookCollaborationRepository {
    /** Whether the current user owns the book with local [bookId] (vs. being a collaborator). */
    suspend fun isOwner(bookId: Long): Boolean

    /**
     * Collaborators of the book with local [bookId], owner first, then accepted members, then
     * pending invites. Empty when the book hasn't synced a remote id yet.
     */
    suspend fun getMembers(bookId: Long): List<RecipeBookMember>

    /** Invites the [email] address to the book with local [bookId] at [role]. */
    suspend fun invite(bookId: Long, email: String, role: RecipeBookRole)

    /** Removes the collaborator / cancels the invite with remote [memberId]. */
    suspend fun removeMember(memberId: String)

    /**
     * Removes the current user's own membership from the book with local [bookId], then drops the
     * book — and the recipes only it held — from the local cache. Owners can't leave their own
     * book; they delete it via [RecipeBookRepository.deleteBook] instead.
     */
    suspend fun leaveBook(bookId: Long)

    /** Pending invites addressed to the current user's email. */
    suspend fun pendingInvites(): List<RecipeBookInvite>

    /**
     * Accepts the invite with remote [memberId], then pulls the now-shared book and its recipes.
     */
    suspend fun acceptInvite(memberId: String)

    /** Declines (deletes) the invite with remote [memberId]. */
    suspend fun declineInvite(memberId: String)
}
