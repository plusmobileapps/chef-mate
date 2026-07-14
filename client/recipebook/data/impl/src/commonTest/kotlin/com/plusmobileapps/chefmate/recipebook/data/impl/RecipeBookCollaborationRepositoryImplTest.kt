@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipebook.data.impl

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.recipe.data.testing.FakeRecipeRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMemberStatus
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RecipeBookMemberRemoteDataSource
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RemoteCollaborator
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RemoteRecipeBookInvite
import com.plusmobileapps.chefmate.recipebook.data.impl.remote.RemoteRecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.testing.FakeRecipeBookRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class RecipeBookCollaborationRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()

    private fun repository(remote: RecipeBookMemberRemoteDataSource) =
        RecipeBookCollaborationRepositoryImpl(
            bookDb = db.recipeBookQueries,
            ioContext = testDispatcher,
            remote = remote,
            authRepository = fakeAuth,
            recipeBookRepository = FakeRecipeBookRepository(),
            recipeRepository = FakeRecipeRepository(),
        )

    /** Creates a synced book (with a remote id) and returns its local id. */
    private fun createSyncedBook(remoteId: String): Long {
        db.recipeBookQueries.create(
            name = "Shared",
            isDefault = false,
            createdAt = "2026-07-13 00:00:00",
            updatedAt = "2026-07-13 00:00:00",
            clientId = "client-1",
            ownerId = "test-id",
        )
        val id = db.recipeBookQueries.getAll().executeAsList().first { it.name == "Shared" }.id
        db.recipeBookQueries.updateRemoteId(remoteId = remoteId, id = id)
        return id
    }

    @Test
    fun declining_an_invite_marks_it_rejected_instead_of_deleting_the_row() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val remote = RecordingMemberRemote()
            val repo = repository(remote)

            repo.declineInvite("member-1")

            // The row is kept (owner still sees it) and stamped rejected — not deleted.
            remote.rejected shouldContainExactly listOf("member-1")
            remote.deleted shouldBe emptyList()
        }

    @Test
    fun getMembers_maps_each_wire_status_to_the_member_status() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val bookId = createSyncedBook(remoteId = "remote-1")
            val remote =
                RecordingMemberRemote(
                    collaborators =
                        listOf(
                            collaborator("owner@example.com", "accepted", isOwner = true),
                            collaborator("alex@example.com", "accepted"),
                            collaborator("sam@example.com", "pending"),
                            collaborator("jordan@example.com", "rejected"),
                        )
                )
            val repo = repository(remote)

            val statusByEmail = repo.getMembers(bookId).associate { it.email to it.status }

            statusByEmail["owner@example.com"] shouldBe RecipeBookMemberStatus.ACCEPTED
            statusByEmail["alex@example.com"] shouldBe RecipeBookMemberStatus.ACCEPTED
            statusByEmail["sam@example.com"] shouldBe RecipeBookMemberStatus.PENDING
            statusByEmail["jordan@example.com"] shouldBe RecipeBookMemberStatus.REJECTED
        }

    private fun collaborator(email: String, status: String, isOwner: Boolean = false) =
        RemoteCollaborator(
            memberId = if (isOwner) null else "m-$email",
            email = email,
            role = if (isOwner) "owner" else "editor",
            status = status,
            isOwner = isOwner,
        )

    private class RecordingMemberRemote(
        private val collaborators: List<RemoteCollaborator> = emptyList()
    ) : RecipeBookMemberRemoteDataSource {
        val rejected: MutableList<String> = mutableListOf()
        val deleted: MutableList<String> = mutableListOf()

        override suspend fun fetchMembers(bookRemoteId: String): List<RemoteRecipeBookMember> =
            emptyList()

        override suspend fun fetchCollaborators(bookRemoteId: String): List<RemoteCollaborator> =
            collaborators

        override suspend fun invite(bookRemoteId: String, email: String, role: String) = Unit

        override suspend fun deleteMember(memberId: String) {
            deleted += memberId
        }

        override suspend fun fetchPendingInvites(email: String): List<RemoteRecipeBookInvite> =
            emptyList()

        override suspend fun acceptInvite(memberId: String, userId: String) = Unit

        override suspend fun rejectInvite(memberId: String) {
            rejected += memberId
        }
    }
}
