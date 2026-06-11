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
        // The RPC returns everyone on the book — owner first, then accepted, then pending — visible
        // to any collaborator. We only know an avatar for the current viewer's own row.
        return remote.fetchCollaborators(remoteId).map {
            RecipeBookMember(
                id = it.memberId,
                email = it.email,
                role = RecipeBookRole.fromWire(it.role),
                accepted = it.status == "accepted",
                isOwner = it.isOwner,
                avatarUrl =
                    if (me != null && it.email.equals(me.userEmail, ignoreCase = true)) {
                        me.userProfileImageUrl
                    } else {
                        null
                    },
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
        remote.deleteMember(memberId)
    }
}
