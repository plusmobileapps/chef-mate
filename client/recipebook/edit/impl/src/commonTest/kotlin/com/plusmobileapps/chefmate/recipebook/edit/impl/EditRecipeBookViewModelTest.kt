@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipebook.edit.impl

import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.testing.FakeRecipeBookCollaborationRepository
import com.plusmobileapps.chefmate.recipebook.data.testing.FakeRecipeBookRepository
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class EditRecipeBookViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun viewModel(
        props: EditRecipeBookBloc.Props,
        repository: FakeRecipeBookRepository = FakeRecipeBookRepository(),
        collaborationRepository: FakeRecipeBookCollaborationRepository =
            FakeRecipeBookCollaborationRepository(),
        onSaved: () -> Unit = {},
    ) =
        EditRecipeBookViewModel(
            props = props,
            onSaved = onSaved,
            mainContext = testDispatcher,
            ioContext = testDispatcher,
            repository = repository,
            collaborationRepository = collaborationRepository,
        )

    @Test
    fun create_starts_with_a_blank_name() =
        runTest(testDispatcher) {
            val vm = viewModel(EditRecipeBookBloc.Props.Create)

            vm.state.value.name shouldBe ""
            vm.state.value.isCreate shouldBe true
        }

    @Test
    fun edit_owner_loads_collaborators_and_can_invite() =
        runTest(testDispatcher) {
            val collab = FakeRecipeBookCollaborationRepository(owner = true)
            val vm =
                viewModel(
                    EditRecipeBookBloc.Props.Edit(bookId = 3),
                    collaborationRepository = collab,
                )

            vm.state.value.canManageCollaborators shouldBe true

            vm.onInviteEmailChanged("alex@example.com")
            vm.onInviteRoleChanged(
                com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole.VIEWER
            )
            vm.onInviteClicked()

            collab.invited.single().second shouldBe "alex@example.com"
            collab.invited.single().third shouldBe
                com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole.VIEWER
            vm.state.value.inviteEmail shouldBe ""
        }

    @Test
    fun removing_a_member_confirms_first_then_calls_repository() =
        runTest(testDispatcher) {
            val collab = FakeRecipeBookCollaborationRepository(owner = true)
            collab.members.add(
                com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember(
                    id = "m1",
                    email = "alex@example.com",
                    role = com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole.EDITOR,
                    accepted = true,
                )
            )
            val vm =
                viewModel(
                    EditRecipeBookBloc.Props.Edit(bookId = 3),
                    collaborationRepository = collab,
                )

            vm.onRemoveMemberClicked("m1")
            // Clicking only opens the confirmation — nothing removed yet.
            vm.state.value.removingMember?.id shouldBe "m1"
            collab.removed.isEmpty() shouldBe true

            vm.onConfirmRemoveMember()

            collab.removed shouldBe listOf("m1")
            vm.state.value.removingMember shouldBe null
        }

    @Test
    fun dismissing_remove_confirmation_does_not_remove() =
        runTest(testDispatcher) {
            val collab = FakeRecipeBookCollaborationRepository(owner = true)
            collab.members.add(
                com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember(
                    id = "m1",
                    email = "alex@example.com",
                    role = com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole.EDITOR,
                    accepted = true,
                )
            )
            val vm =
                viewModel(
                    EditRecipeBookBloc.Props.Edit(bookId = 3),
                    collaborationRepository = collab,
                )

            vm.onRemoveMemberClicked("m1")
            vm.onDismissRemoveMember()

            vm.state.value.removingMember shouldBe null
            collab.removed.isEmpty() shouldBe true
        }

    @Test
    fun invalid_invite_email_sets_an_error_and_does_not_invite() =
        runTest(testDispatcher) {
            val collab = FakeRecipeBookCollaborationRepository(owner = true)
            val vm =
                viewModel(
                    EditRecipeBookBloc.Props.Edit(bookId = 3),
                    collaborationRepository = collab,
                )

            vm.onInviteEmailChanged("not-an-email")
            vm.onInviteClicked()

            (vm.state.value.inviteError != null) shouldBe true
            collab.invited.isEmpty() shouldBe true
        }

    @Test
    fun edit_loads_the_existing_book_name() =
        runTest(testDispatcher) {
            val book =
                RecipeBook(
                    id = 7,
                    name = "Holiday Baking",
                    createdAt = Instant.DISTANT_PAST,
                    updatedAt = Instant.DISTANT_PAST,
                )
            val repo = FakeRecipeBookRepository(MutableStateFlow(listOf(book)))

            val vm = viewModel(EditRecipeBookBloc.Props.Edit(book.id), repo)

            vm.state.value.name shouldBe "Holiday Baking"
            vm.state.value.isCreate shouldBe false
        }

    @Test
    fun blank_name_sets_an_error_and_does_not_finish() =
        runTest(testDispatcher) {
            var finished = false
            val vm = viewModel(EditRecipeBookBloc.Props.Create, onSaved = { finished = true })

            vm.onNameChanged("   ")
            vm.onSaveClicked()

            (vm.state.value.nameError != null) shouldBe true
            finished shouldBe false
        }

    @Test
    fun saving_a_create_persists_the_book_and_finishes() =
        runTest(testDispatcher) {
            var finished = false
            val repo = FakeRecipeBookRepository(MutableStateFlow(emptyList()))
            val vm = viewModel(EditRecipeBookBloc.Props.Create, repo, onSaved = { finished = true })

            vm.onNameChanged("Grill Nights")
            vm.onSaveClicked()

            finished shouldBe true
        }
}
