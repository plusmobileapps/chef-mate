@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.family.data.impl

import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.database.Database
import com.plusmobileapps.chefmate.database.testing.createTestDatabase
import com.plusmobileapps.chefmate.family.data.AlreadyInFamilyException
import com.plusmobileapps.chefmate.family.data.FamilyMemberStatus
import com.plusmobileapps.chefmate.family.data.FamilyRole
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamily
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyCollaborator
import com.plusmobileapps.chefmate.family.data.remote.RemoteFamilyInvite
import com.plusmobileapps.chefmate.family.data.testing.FakeFamilyRemoteDataSource
import com.plusmobileapps.chefmate.util.testing.FakeDateTimeUtil
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class FamilyRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val db: Database = createTestDatabase()
    private val fakeAuth = FakeAuthenticationRepository()
    private val remote = FakeFamilyRemoteDataSource()

    private fun repository() =
        FamilyRepositoryImpl(
            familyQueries = db.familyQueries,
            memberQueries = db.familyMemberQueries,
            ioContext = testDispatcher,
            dateTimeUtil = FakeDateTimeUtil(),
            remote = remote,
            authRepository = fakeAuth,
        )

    @Test
    fun When_signed_out_Then_there_is_no_family() =
        runTest(testDispatcher) {
            val repo = repository()

            repo.family.value shouldBe null
            repo.members().first() shouldBe emptyList()
        }

    @Test
    fun When_creating_a_family_Then_it_is_cached_and_owned_by_the_current_user() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val userId = currentUserId()
            val repo = repository()

            repo.createFamily("  The Hendersons  ")

            val family = repo.family.value
            family shouldNotBe null
            // The name is trimmed before it reaches the server.
            family!!.name shouldBe "The Hendersons"
            family.remoteId shouldBe "family-remote-1"
            // The fake stamps the caller as owner, so the local row resolves to owned-by-me.
            remote.currentFamily?.ownerId shouldBe userId
        }

    @Test
    fun When_already_in_a_family_Then_creating_another_is_rejected() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")

            shouldThrow<AlreadyInFamilyException> { repo.createFamily("Second Family") }
        }

    @Test
    fun When_already_in_a_family_Then_accepting_an_invite_is_rejected() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")
            remote.pendingInvites +=
                RemoteFamilyInvite(
                    memberId = "m1",
                    familyId = "other-family",
                    familyName = "The Other Family",
                    role = "member",
                    status = "pending",
                )

            shouldThrow<AlreadyInFamilyException> { repo.acceptInvite("m1") }
        }

    @Test
    fun When_syncing_Then_members_are_replaced_wholesale_so_removals_do_not_linger() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            remote.currentFamily =
                RemoteFamily(id = "family-remote-1", name = "The Hendersons", ownerId = "test-id")
            remote.members =
                mutableListOf(
                    ownerRow(),
                    memberRow(id = "m1", email = "alex@example.com"),
                    memberRow(id = "m2", email = "sam@example.com"),
                )

            repo.refresh()
            repo.members().first().map { it.email } shouldBe
                listOf("owner@example.com", "alex@example.com", "sam@example.com")

            // Someone removed m2 on another device.
            remote.members =
                mutableListOf(ownerRow(), memberRow(id = "m1", email = "alex@example.com"))
            repo.refresh()

            repo.members().first().map { it.email } shouldBe
                listOf("owner@example.com", "alex@example.com")
        }

    @Test
    fun When_the_server_reports_no_family_Then_the_local_cache_is_dropped() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")
            repo.family.value shouldNotBe null

            // Removed from the family on another device.
            remote.currentFamily = null
            repo.refresh()

            repo.family.value shouldBe null
            repo.members().first() shouldBe emptyList()
        }

    @Test
    fun When_inviting_Then_the_address_is_normalised_before_it_is_sent() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")

            repo.invite("  Alex@Example.COM ")

            remote.invitedEmails shouldBe listOf("alex@example.com")
        }

    @Test
    fun When_a_member_is_pending_Then_the_status_and_role_survive_the_round_trip() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            remote.currentFamily =
                RemoteFamily(id = "family-remote-1", name = "The Hendersons", ownerId = "test-id")
            remote.members = mutableListOf(ownerRow(), memberRow(id = "m1", status = "pending"))

            repo.refresh()

            val member = repo.members().first().single { !it.isOwner }
            member.status shouldBe FamilyMemberStatus.PENDING
            member.role shouldBe FamilyRole.MEMBER
            member.id shouldBe "m1"
        }

    @Test
    fun When_leaving_Then_the_local_cache_is_dropped_immediately() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")

            repo.leaveFamily()

            remote.leftFamilyId shouldBe "family-remote-1"
            repo.family.value shouldBe null
        }

    @Test
    fun When_signing_out_Then_cached_family_state_is_cleared() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")

            repo.clearLocalData()

            repo.family.value shouldBe null
            repo.members().first() shouldBe emptyList()
            repo.pendingInvites().first() shouldBe emptyList()
        }

    @Test
    fun When_a_sync_fails_Then_the_previous_cache_is_left_intact() =
        runTest(testDispatcher) {
            fakeAuth.setAuthenticated()
            val repo = repository()
            repo.createFamily("The Hendersons")

            remote.errorToThrow = RuntimeException("offline")
            repo.refresh()

            repo.family.value?.name shouldBe "The Hendersons"
        }

    private fun currentUserId(): String =
        (fakeAuth.state.value as com.plusmobileapps.chefmate.auth.data.AuthState.Authenticated)
            .user
            .userId

    private fun ownerRow() =
        RemoteFamilyCollaborator(
            memberId = null,
            email = "owner@example.com",
            name = "Owner",
            role = "owner",
            status = "accepted",
            isOwner = true,
        )

    private fun memberRow(
        id: String,
        email: String = "member@example.com",
        status: String = "accepted",
    ) =
        RemoteFamilyCollaborator(
            memberId = id,
            email = email,
            name = null,
            role = "member",
            status = status,
            isOwner = false,
        )
}
