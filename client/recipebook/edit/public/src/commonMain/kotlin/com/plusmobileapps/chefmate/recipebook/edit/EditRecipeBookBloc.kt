package com.plusmobileapps.chefmate.recipebook.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface EditRecipeBookBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        EditRecipeBookScreen(bloc = this, modifier = modifier)
    }

    fun onNameChanged(name: String)

    fun onSaveClicked()

    fun onCloseClicked()

    fun onInviteEmailChanged(email: String)

    fun onInviteRoleChanged(role: RecipeBookRole)

    fun onInviteClicked()

    /** Owner tapped remove on a collaborator — asks for confirmation first. */
    fun onRemoveMemberClicked(memberId: String)

    /** Confirms the pending removal. */
    fun onConfirmRemoveMember()

    /** Dismisses the remove-confirmation dialog without removing. */
    fun onDismissRemoveMember()

    /** Owner tapped "Delete book" — asks for confirmation first. */
    fun onDeleteBookClicked()

    /** Collaborator tapped "Leave book" — asks for confirmation first. */
    fun onLeaveBookClicked()

    /** Confirms the pending delete or leave. */
    fun onConfirmBookAction()

    /** Dismisses the delete/leave confirmation dialog without acting. */
    fun onDismissBookAction()

    /** The destructive action awaiting confirmation in [Model.pendingBookAction]. */
    enum class BookAction {
        /** The owner is deleting the book for everyone. */
        DELETE,
        /** A collaborator is removing themselves from the book. */
        LEAVE,
    }

    data class Model(
        val title: TextData,
        val name: String = "",
        val isCreate: Boolean = true,
        val isSaving: Boolean = false,
        val nameError: TextData? = null,
        /** True when editing a book the current user owns — gates the collaborators section. */
        val canManageCollaborators: Boolean = false,
        val members: List<RecipeBookMember> = emptyList(),
        val inviteEmail: String = "",
        val inviteRole: RecipeBookRole = RecipeBookRole.EDITOR,
        val isInviting: Boolean = false,
        val inviteError: TextData? = null,
        /** The collaborator awaiting remove confirmation; non-null shows the confirm dialog. */
        val removingMember: RecipeBookMember? = null,
        /** True for a saved book the user owns that isn't the undeletable default one. */
        val canDeleteBook: Boolean = false,
        /** True for a saved book the user collaborates on rather than owns. */
        val canLeaveBook: Boolean = false,
        /** True while the delete or leave is in flight. */
        val isRemovingBook: Boolean = false,
        /** Non-null shows the delete/leave confirmation dialog. */
        val pendingBookAction: BookAction? = null,
        /** Set when the delete or leave failed, e.g. offline. */
        val bookActionError: TextData? = null,
    ) {
        val canSave: Boolean
            get() = name.isNotBlank() && !isSaving
    }

    sealed class Output {
        /** The book was created or renamed (or the user closed the modal). Pop the modal. */
        data object Finished : Output()
    }

    /**
     * Drives create vs. edit. [Create] opens a blank form; [Edit] loads the book's current name.
     */
    @Serializable
    sealed class Props {
        @Serializable data object Create : Props()

        @Serializable data class Edit(val bookId: Long) : Props()
    }

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): EditRecipeBookBloc
    }
}
