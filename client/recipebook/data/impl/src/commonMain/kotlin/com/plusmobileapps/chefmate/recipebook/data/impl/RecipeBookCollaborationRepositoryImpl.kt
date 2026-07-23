package com.plusmobileapps.chefmate.recipebook.data.impl

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.database.RecipeBookQueries
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookCollaborationRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookInvite
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMemberStatus
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RecipeBookMemberRemoteDataSource
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
@Inject
@ContributesBinding(AppScope::class)
class RecipeBookCollaborationRepositoryImpl(
    private val bookDb: RecipeBookQueries,
    @IO private val ioContext: CoroutineContext,
    private val remote: RecipeBookMemberRemoteDataSource,
    private val authRepository: AuthenticationRepository,
    private val recipeBookRepository: RecipeBookRepository,
    private val recipeRepository: RecipeRepository,
) : RecipeBookCollaborationRepository {

    private val currentUser: ChefMateUser?
        get() = (authRepository.state.value as? AuthState.Authenticated)?.user

    private suspend fun bookRemoteId(bookId: Long): String? =
        withContext(ioContext) { bookDb.getById(bookId).executeAsOneOrNull()?.remoteId }

    override suspend fun isOwner(bookId: Long): Boolean {
        val book =
            withContext(ioContext) { bookDb.getById(bookId).executeAsOneOrNull() } ?: return true
        // Locally-created books carry no ownerId until first sync — those are owned by the creator.
        return book.ownerId == null || book.ownerId == currentUser?.userId
    }

    override suspend fun getMembers(bookId: Long): List<RecipeBookMember> {
        val remoteId = bookRemoteId(bookId) ?: return emptyList()
        val me = currentUser
        // The RPC returns everyone on the book — owner first, then accepted, then pending — with
        // each person's avatar from their account metadata (null for pending invites). Prefer the
        // local profile image for the current user's own row in case it's fresher than the server.
        return remote.fetchCollaborators(remoteId).map {
            val isCurrentUser = me != null && it.email.equals(me.userEmail, ignoreCase = true)
            RecipeBookMember(
                id = it.memberId,
                email = it.email,
                role = RecipeBookRole.fromWire(it.role),
                status =
                    when (it.status) {
                        "accepted" -> RecipeBookMemberStatus.ACCEPTED
                        "rejected" -> RecipeBookMemberStatus.REJECTED
                        else -> RecipeBookMemberStatus.PENDING
                    },
                name = if (isCurrentUser) me.userName else it.name,
                isOwner = it.isOwner,
                avatarUrl =
                    if (isCurrentUser) me.userProfileImageUrl ?: it.avatarUrl else it.avatarUrl,
            )
        }
    }

    override suspend fun invite(bookId: Long, email: String, role: RecipeBookRole) {
        val remoteId = bookRemoteId(bookId) ?: error("Book not synced yet")
        // Normalise the address so it always matches the invitee's (lowercased) account email.
        remote.invite(
            bookRemoteId = remoteId,
            email = email.trim().lowercase(),
            role = role.wireValue,
        )
    }

    override suspend fun removeMember(memberId: String) {
        remote.deleteMember(memberId)
    }

    override suspend fun leaveBook(bookId: Long) {
        val email = currentUser?.userEmail?.trim()?.lowercase() ?: error("Not signed in")
        val remoteId = bookRemoteId(bookId) ?: error("Book not synced yet")
        // Find our own member row. The owner has no member row of their own, so a match here also
        // proves we're a collaborator rather than the owner.
        val membership =
            remote.fetchCollaborators(remoteId).firstOrNull {
                !it.isOwner && it.email.trim().lowercase() == email
            } ?: error("Not a collaborator on book $bookId")
        remote.deleteMember(membership.memberId ?: error("Membership is missing its id"))
        // Access is gone the moment the member row is deleted, so purge the local copy: the
        // recipes this book alone held, then the book itself.
        recipeRepository.deleteLocalRecipesInBook(bookId)
        recipeBookRepository.removeLocalBook(bookId)
    }

    override suspend fun pendingInvites(): List<RecipeBookInvite> {
        val email = currentUser?.userEmail?.trim()?.lowercase() ?: return emptyList()
        return remote.fetchPendingInvites(email).map {
            RecipeBookInvite(
                memberId = it.id,
                bookName = it.book.name,
                role = RecipeBookRole.fromWire(it.role),
            )
        }
    }

    override suspend fun acceptInvite(memberId: String) {
        val userId = currentUser?.userId ?: return
        remote.acceptInvite(memberId = memberId, userId = userId)
        // Pull the now-accessible book and its recipes into the local cache.
        recipeBookRepository.syncAllUnsynced()
        recipeRepository.syncAllUnsynced()
    }

    override suspend fun declineInvite(memberId: String) {
        // Mark the invite rejected rather than deleting the row, so the owner can see the recipient
        // turned it down. The invitee loses book access either way (access requires status =
        // 'accepted'); the owner can still remove the row later to clear it out.
        remote.rejectInvite(memberId)
    }
}
