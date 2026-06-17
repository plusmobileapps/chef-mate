package com.plusmobileapps.chefmate.grocery.core.edit

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow

interface EditGroceryListBloc : BackClickBloc, ComposeScreen {
    val state: StateFlow<Model>

    fun onNameChanged(name: String)

    fun onRenameSubmitted()

    fun onDeleteClicked()

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    fun onInviteEmailChanged(email: String)

    fun onInviteRoleChanged(role: ListRole)

    fun onInviteClicked()

    /** Asks for confirmation before removing; shows the confirm dialog. */
    fun onRemoveCollaboratorClicked(collaborator: ListCollaborator)

    fun onConfirmRemoveCollaborator()

    fun onDismissRemoveCollaborator()

    fun onSignInClicked()

    fun onSignUpClicked()

    /**
     * @param name current (possibly edited) list name shown in the rename field.
     * @param isAuthenticated true only for a real, non-anonymous account — gates the collaboration
     *   UI. Anonymous Supabase sessions count as unauthenticated here.
     * @param isOwner whether the current user owns the list; only owners may delete or invite.
     * @param collaborators current members of the list (owner + editors/viewers, incl. pending).
     * @param showDeleteConfirm whether the delete-confirmation dialog is visible.
     * @param collaboratorPendingRemoval the collaborator awaiting remove confirmation, or null.
     */
    data class Model(
        val name: String = "",
        val isAuthenticated: Boolean = false,
        val isOwner: Boolean = true,
        val collaborators: List<ListCollaborator> = emptyList(),
        val showDeleteConfirm: Boolean = false,
        val collaboratorPendingRemoval: ListCollaborator? = null,
        val inviteEmail: String = "",
        val inviteRole: ListRole = ListRole.EDITOR,
        val isInviting: Boolean = false,
        val inviteError: TextData? = null,
    )

    sealed class Output {
        data object Finished : Output()

        data object OpenSignIn : Output()

        data object OpenSignUp : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            listId: Long,
            output: Consumer<Output>,
        ): EditGroceryListBloc
    }
}
