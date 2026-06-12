@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.grocery.core.impl.edit

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryListModel
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class EditGroceryListBlocTest {
    private val context = TestBlocContext.create()
    private val output = TestConsumer<EditGroceryListBloc.Output>()
    private val authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private val lists =
        MutableStateFlow(
            listOf(GroceryListModel(id = LIST_ID, name = "Groceries", role = ListRole.OWNER))
        )

    private val repository =
        mock<GroceryRepository>(MockMode.autoUnit) {
            every { getGroceryLists() } returns lists
            every { getListCollaborators(LIST_ID) } returns flowOf(emptyList())
        }

    private val authRepository =
        mock<AuthenticationRepository>(MockMode.autoUnit) { every { state } returns authState }

    private fun bloc() =
        EditGroceryListBlocImpl(
            context = context,
            listId = LIST_ID,
            output = output,
            repository = repository,
            authRepository = authRepository,
        )

    private fun authenticatedUser(isAnonymous: Boolean) =
        AuthState.Authenticated(
            ChefMateUser(
                userId = "u1",
                userName = "User",
                userEmail = "user@example.com",
                userProfileImageUrl = null,
                isAnonymous = isAnonymous,
            )
        )

    @Test
    fun When_loaded_Then_name_and_owner_are_seeded_from_list() = runTest {
        bloc().state.test {
            val model = awaitItem()
            model.name shouldBe "Groceries"
            model.isOwner shouldBe true
            model.isAuthenticated shouldBe false
        }
    }

    @Test
    fun When_unauthenticated_Then_collaboration_is_gated() = runTest {
        bloc().state.test { awaitItem().isAuthenticated shouldBe false }
    }

    @Test
    fun When_authenticated_with_real_account_Then_collaboration_is_enabled() = runTest {
        authState.value = authenticatedUser(isAnonymous = false)
        bloc().state.test { awaitItem().isAuthenticated shouldBe true }
    }

    @Test
    fun When_authenticated_anonymously_Then_collaboration_stays_gated() = runTest {
        authState.value = authenticatedUser(isAnonymous = true)
        bloc().state.test { awaitItem().isAuthenticated shouldBe false }
    }

    @Test
    fun When_delete_confirmed_Then_list_deleted_and_finished_emitted() = runTest {
        val bloc = bloc()
        bloc.state.test { awaitItem() }
        bloc.onDeleteClicked()
        bloc.onDeleteConfirmed()
        verifySuspend { repository.deleteGroceryList(LIST_ID) }
        output.lastValue shouldBe EditGroceryListBloc.Output.Finished
    }

    @Test
    fun When_rename_submitted_Then_repository_renamed() = runTest {
        val bloc = bloc()
        bloc.state.test { awaitItem() }
        bloc.onNameChanged("Party Supplies")
        bloc.onRenameSubmitted()
        verifySuspend { repository.renameGroceryList(LIST_ID, "Party Supplies") }
    }

    @Test
    fun When_invite_Then_repository_invited_as_editor() = runTest {
        val bloc = bloc()
        bloc.state.test { awaitItem() }
        bloc.onInviteCollaborator("friend@example.com")
        verifySuspend {
            repository.inviteCollaborator(LIST_ID, "friend@example.com", ListRole.EDITOR)
        }
    }

    @Test
    fun When_sign_in_clicked_Then_open_sign_in_emitted() = runTest {
        val bloc = bloc()
        bloc.onSignInClicked()
        output.lastValue shouldBe EditGroceryListBloc.Output.OpenSignIn
    }

    @Test
    fun When_back_clicked_Then_finished_emitted() = runTest {
        val bloc = bloc()
        bloc.onBackClicked()
        output.lastValue shouldBe EditGroceryListBloc.Output.Finished
    }

    companion object {
        private const val LIST_ID = 1L
    }
}
