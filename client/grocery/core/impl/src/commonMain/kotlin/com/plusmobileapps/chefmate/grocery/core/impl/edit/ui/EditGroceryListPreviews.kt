package com.plusmobileapps.chefmate.grocery.core.impl.edit.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListScreen
import com.plusmobileapps.chefmate.grocery.data.CollaborationStatus
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private val sampleCollaborators =
    listOf(
        ListCollaborator(
            id = 1L,
            email = "owner@example.com",
            displayName = "You",
            role = ListRole.OWNER,
            status = CollaborationStatus.ACCEPTED,
        ),
        ListCollaborator(
            id = 2L,
            email = "alex@example.com",
            displayName = "Alex Rivera",
            role = ListRole.EDITOR,
            status = CollaborationStatus.ACCEPTED,
        ),
        ListCollaborator(
            id = 3L,
            email = "pending@example.com",
            displayName = null,
            role = ListRole.EDITOR,
            status = CollaborationStatus.PENDING,
        ),
        ListCollaborator(
            id = 4L,
            email = "sam@example.com",
            displayName = "Sam Lee",
            role = ListRole.VIEWER,
            status = CollaborationStatus.ACCEPTED,
        ),
    )

private fun fakeEditBloc(model: EditGroceryListBloc.Model): EditGroceryListBloc =
    object : EditGroceryListBloc {
        override val state = MutableStateFlow(model)

        override fun onNameChanged(name: String) = Unit

        override fun onRenameSubmitted() = Unit

        override fun onDeleteClicked() = Unit

        override fun onDeleteConfirmed() = Unit

        override fun onDeleteDismissed() = Unit

        override fun onInviteEmailChanged(email: String) = Unit

        override fun onInviteRoleChanged(role: ListRole) = Unit

        override fun onInviteClicked() = Unit

        override fun onRemoveCollaboratorClicked(collaborator: ListCollaborator) = Unit

        override fun onConfirmRemoveCollaborator() = Unit

        override fun onDismissRemoveCollaborator() = Unit

        override fun onSignInClicked() = Unit

        override fun onSignUpClicked() = Unit

        override fun onBackClicked() = Unit
    }

/** Authenticated owner managing collaborators. */
val previewEditGroceryListBlocAuthenticated: EditGroceryListBloc =
    fakeEditBloc(
        EditGroceryListBloc.Model(
            name = "Weekly Groceries",
            isAuthenticated = true,
            isOwner = true,
            collaborators = sampleCollaborators,
        )
    )

/** Signed-out state: collaboration gated behind sign in / sign up. */
val previewEditGroceryListBlocUnauthenticated: EditGroceryListBloc =
    fakeEditBloc(
        EditGroceryListBloc.Model(
            name = "Weekly Groceries",
            isAuthenticated = false,
            isOwner = true,
        )
    )

/** Delete confirmation dialog visible. */
val previewEditGroceryListBlocDeleteConfirm: EditGroceryListBloc =
    fakeEditBloc(
        EditGroceryListBloc.Model(
            name = "Weekly Groceries",
            isAuthenticated = true,
            isOwner = true,
            collaborators = sampleCollaborators,
            showDeleteConfirm = true,
        )
    )

@Preview
@Composable
internal fun EditGroceryListAuthenticatedPreview() {
    ChefMateTheme { EditGroceryListScreen(bloc = previewEditGroceryListBlocAuthenticated) }
}

@Preview
@Composable
internal fun EditGroceryListUnauthenticatedPreview() {
    ChefMateTheme { EditGroceryListScreen(bloc = previewEditGroceryListBlocUnauthenticated) }
}
