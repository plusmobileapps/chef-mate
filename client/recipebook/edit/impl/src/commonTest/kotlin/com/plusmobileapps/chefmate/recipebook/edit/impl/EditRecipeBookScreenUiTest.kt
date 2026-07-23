@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipebook.edit.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookScreen
import com.plusmobileapps.chefmate.recipebook.edit.robots.editRecipeBook
import com.plusmobileapps.chefmate.text.FixedString
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow

class EditRecipeBookScreenUiTest {

    @Test
    fun typing_a_name_and_saving_drives_the_bloc() = runComposeUiTest {
        val bloc = RecordingEditBloc()
        setContent { EditRecipeBookScreen(bloc = bloc) }

        editRecipeBook().assertDisplayed().typeName("Grill Nights").save()

        bloc.lastName shouldBe "Grill Nights"
        bloc.saveClicks shouldBe 1
    }

    @Test
    fun an_owner_sees_delete_and_can_confirm_it() = runComposeUiTest {
        val bloc =
            RecordingEditBloc(EditRecipeBookBloc.Model(FixedString("Edit"), canDeleteBook = true))
        setContent { EditRecipeBookScreen(bloc = bloc) }

        editRecipeBook().assertLeaveBookNotShown().deleteBook()

        bloc.deleteClicks shouldBe 1

        bloc.state.value =
            bloc.state.value.copy(pendingBookAction = EditRecipeBookBloc.BookAction.DELETE)
        editRecipeBook().confirmDialog("Delete")

        bloc.confirmClicks shouldBe 1
    }

    @Test
    fun a_collaborator_sees_leave_instead_of_delete() = runComposeUiTest {
        val bloc =
            RecordingEditBloc(EditRecipeBookBloc.Model(FixedString("Edit"), canLeaveBook = true))
        setContent { EditRecipeBookScreen(bloc = bloc) }

        editRecipeBook().assertDeleteBookNotShown().leaveBook()

        bloc.leaveClicks shouldBe 1
    }

    private class RecordingEditBloc(
        model: EditRecipeBookBloc.Model =
            EditRecipeBookBloc.Model(title = FixedString("New recipe book"), name = "x")
    ) : EditRecipeBookBloc {
        var lastName: String? = null
        var saveClicks: Int = 0
        var deleteClicks: Int = 0
        var leaveClicks: Int = 0
        var confirmClicks: Int = 0

        override val state = MutableStateFlow(model)

        override fun onNameChanged(name: String) {
            lastName = name
            state.value = state.value.copy(name = name)
        }

        override fun onSaveClicked() {
            saveClicks++
        }

        override fun onCloseClicked() = Unit

        override fun onInviteEmailChanged(email: String) = Unit

        override fun onInviteRoleChanged(
            role: com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
        ) = Unit

        override fun onInviteClicked() = Unit

        override fun onRemoveMemberClicked(memberId: String) = Unit

        override fun onConfirmRemoveMember() = Unit

        override fun onDismissRemoveMember() = Unit

        override fun onDeleteBookClicked() {
            deleteClicks++
        }

        override fun onLeaveBookClicked() {
            leaveClicks++
        }

        override fun onConfirmBookAction() {
            confirmClicks++
        }

        override fun onDismissBookAction() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            EditRecipeBookScreen(bloc = this, modifier = modifier)
        }
    }
}
