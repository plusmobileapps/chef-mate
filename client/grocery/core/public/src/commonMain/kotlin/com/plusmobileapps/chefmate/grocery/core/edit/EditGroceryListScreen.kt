package com.plusmobileapps.chefmate.grocery.core.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_cancel
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_declined
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_group_editors
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_group_owners
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_group_viewers
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_pending
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_signed_out_message
import chefmate.client.grocery.core.public.generated.resources.grocery_collab_signed_out_title
import chefmate.client.grocery.core.public.generated.resources.grocery_collaborators
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_list
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_list_confirm
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_list_confirm_message
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_list_confirm_title
import chefmate.client.grocery.core.public.generated.resources.grocery_edit_list_title
import chefmate.client.grocery.core.public.generated.resources.grocery_invite
import chefmate.client.grocery.core.public.generated.resources.grocery_invite_hint
import chefmate.client.grocery.core.public.generated.resources.grocery_list_name_label
import chefmate.client.grocery.core.public.generated.resources.grocery_remove_collaborator
import chefmate.client.grocery.core.public.generated.resources.grocery_remove_collaborator_message
import chefmate.client.grocery.core.public.generated.resources.grocery_remove_collaborator_title
import chefmate.client.grocery.core.public.generated.resources.grocery_rename_save
import chefmate.client.grocery.core.public.generated.resources.grocery_role_editor
import chefmate.client.grocery.core.public.generated.resources.grocery_role_owner
import chefmate.client.grocery.core.public.generated.resources.grocery_role_viewer
import chefmate.client.grocery.core.public.generated.resources.grocery_sign_in
import chefmate.client.grocery.core.public.generated.resources.grocery_sign_up
import com.plusmobileapps.chefmate.grocery.data.CollaborationStatus
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusAvatar
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditGroceryListScreen(bloc: EditGroceryListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    val dimens = ChefMateTheme.dimens

    PlusHeaderContainer(
        modifier = modifier.testTag(EditGroceryListTestTags.SCREEN).fillMaxWidth().imePadding(),
        data =
            PlusHeaderData.Modal(
                title = Res.string.grocery_edit_list_title.asTextData(),
                onCloseClick = bloc::onBackClicked,
            ),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(dimens.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingNormal),
            ) {
                PlusTextField(
                    value = state.name,
                    onValueChange = bloc::onNameChanged,
                    label = { Text(stringResource(Res.string.grocery_list_name_label)) },
                    singleLine = true,
                    enabled = state.isOwner,
                    modifier = Modifier.fillMaxWidth().testTag(EditGroceryListTestTags.NAME_FIELD),
                )
                if (state.isOwner) {
                    PlusButton(
                        text = Res.string.grocery_rename_save.asTextData(),
                        variant = PlusButtonVariant.SECONDARY,
                        enabled = state.name.isNotBlank(),
                        onClick = bloc::onRenameSubmitted,
                    )
                }
            }

            HorizontalDivider()

            CollaborationSection(state = state, bloc = bloc)

            if (state.isOwner) {
                HorizontalDivider()
                PlusButton(
                    text = Res.string.grocery_delete_list.asTextData(),
                    variant = PlusButtonVariant.DESTRUCTIVE,
                    onClick = bloc::onDeleteClicked,
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(dimens.paddingNormal)
                            .testTag(EditGroceryListTestTags.DELETE_BUTTON),
                )
            }
        },
    )

    if (state.showDeleteConfirm) {
        PlusDialog(
            title = Res.string.grocery_delete_list_confirm_title.asTextData(),
            message = Res.string.grocery_delete_list_confirm_message.asTextData(),
            confirmButtonText = Res.string.grocery_delete_list_confirm.asTextData(),
            dismissButtonText = Res.string.grocery_cancel.asTextData(),
            onConfirmClick = bloc::onDeleteConfirmed,
            onDismissRequest = bloc::onDeleteDismissed,
        )
    }

    if (state.collaboratorPendingRemoval != null) {
        PlusDialog(
            title = Res.string.grocery_remove_collaborator_title.asTextData(),
            message = Res.string.grocery_remove_collaborator_message.asTextData(),
            confirmButtonText = Res.string.grocery_remove_collaborator.asTextData(),
            dismissButtonText = Res.string.grocery_cancel.asTextData(),
            onConfirmClick = bloc::onConfirmRemoveCollaborator,
            onDismissRequest = bloc::onDismissRemoveCollaborator,
        )
    }
}

@Composable
private fun CollaborationSection(state: EditGroceryListBloc.Model, bloc: EditGroceryListBloc) {
    val dimens = ChefMateTheme.dimens
    Column(
        modifier = Modifier.fillMaxWidth().padding(dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        Text(
            text = stringResource(Res.string.grocery_collaborators),
            style = MaterialTheme.typography.titleMedium,
        )

        if (!state.isAuthenticated) {
            Text(
                text = stringResource(Res.string.grocery_collab_signed_out_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(Res.string.grocery_collab_signed_out_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall)) {
                PlusButton(
                    text = Res.string.grocery_sign_in.asTextData(),
                    onClick = bloc::onSignInClicked,
                    modifier = Modifier.testTag(EditGroceryListTestTags.SIGN_IN_BUTTON),
                )
                PlusButton(
                    text = Res.string.grocery_sign_up.asTextData(),
                    variant = PlusButtonVariant.SECONDARY,
                    onClick = bloc::onSignUpClicked,
                    modifier = Modifier.testTag(EditGroceryListTestTags.SIGN_UP_BUTTON),
                )
            }
            return@Column
        }

        // Grouped by role so the owner is separated from editors and viewers. Pending invites keep
        // their invited role, so they land in the matching group (dimmed).
        MemberGroup(
            title = Res.string.grocery_collab_group_owners,
            collaborators = state.collaborators.filter { it.role == ListRole.OWNER },
            canManage = state.isOwner,
            onRemove = bloc::onRemoveCollaboratorClicked,
        )
        MemberGroup(
            title = Res.string.grocery_collab_group_editors,
            collaborators = state.collaborators.filter { it.role == ListRole.EDITOR },
            canManage = state.isOwner,
            onRemove = bloc::onRemoveCollaboratorClicked,
        )
        MemberGroup(
            title = Res.string.grocery_collab_group_viewers,
            collaborators = state.collaborators.filter { it.role == ListRole.VIEWER },
            canManage = state.isOwner,
            onRemove = bloc::onRemoveCollaboratorClicked,
        )

        // Invite controls are owner-only; collaborators just see the list above.
        if (state.isOwner) {
            InviteRow(state = state, bloc = bloc)
        }
    }
}

@Composable
private fun MemberGroup(
    title: StringResource,
    collaborators: List<ListCollaborator>,
    canManage: Boolean,
    onRemove: (ListCollaborator) -> Unit,
) {
    if (collaborators.isEmpty()) return
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = ChefMateTheme.dimens.paddingSmall),
    )
    collaborators.forEach { collaborator ->
        MemberRow(collaborator = collaborator, canManage = canManage, onRemove = onRemove)
    }
}

@Composable
private fun MemberRow(
    collaborator: ListCollaborator,
    canManage: Boolean,
    onRemove: (ListCollaborator) -> Unit,
) {
    // name → email, with pending/declined invites dimmed and tagged. Role is conveyed by the group
    // header above, so it isn't repeated per row. Pending invites have no account yet, so they fall
    // back to the email as the primary line. A declined invite stays visible so the owner can see
    // the person turned it down (and can then remove or re-invite them).
    val dimens = ChefMateTheme.dimens
    val name = collaborator.displayName?.takeIf { it.isNotBlank() }
    val pending = collaborator.status == CollaborationStatus.PENDING
    val declined = collaborator.status == CollaborationStatus.REJECTED
    val secondary =
        listOfNotNull(
                collaborator.email.takeIf { name != null },
                stringResource(Res.string.grocery_collab_pending).takeIf { pending },
                stringResource(Res.string.grocery_collab_declined).takeIf { declined },
            )
            .joinToString(" · ")
    Row(
        modifier = Modifier.fillMaxWidth().alpha(if (pending || declined) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        PlusAvatar(
            imageUrl = collaborator.avatarUrl,
            contentDescription = null,
            fallbackText = name ?: collaborator.email,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name ?: collaborator.email, style = MaterialTheme.typography.bodyMedium)
            if (secondary.isNotEmpty()) {
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (canManage && collaborator.role != ListRole.OWNER) {
            IconButton(onClick = { onRemove(collaborator) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.grocery_remove_collaborator),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InviteRow(state: EditGroceryListBloc.Model, bloc: EditGroceryListBloc) {
    val dimens = ChefMateTheme.dimens
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = dimens.paddingSmall),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        PlusTextField(
            value = state.inviteEmail,
            onValueChange = bloc::onInviteEmailChanged,
            label = { Text(stringResource(Res.string.grocery_invite_hint)) },
            singleLine = true,
            error = state.inviteError,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Send,
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        bloc.onInviteClicked()
                        focusManager.clearFocus()
                    }
                ),
            modifier = Modifier.fillMaxWidth().testTag(EditGroceryListTestTags.INVITE_FIELD),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall)) {
            listOf(ListRole.EDITOR, ListRole.VIEWER).forEach { role ->
                FilterChip(
                    selected = state.inviteRole == role,
                    onClick = { bloc.onInviteRoleChanged(role) },
                    label = { Text(role.label()) },
                )
            }
        }
        PlusButton(
            text = Res.string.grocery_invite.asTextData(),
            variant = PlusButtonVariant.SECONDARY,
            isLoading = state.isInviting,
            enabled = state.inviteEmail.isNotBlank() && !state.isInviting,
            onClick = bloc::onInviteClicked,
            modifier = Modifier.fillMaxWidth().testTag(EditGroceryListTestTags.INVITE_BUTTON),
        )
    }
}

@Composable
private fun ListRole.label(): String =
    stringResource(
        when (this) {
            ListRole.OWNER -> Res.string.grocery_role_owner
            ListRole.EDITOR -> Res.string.grocery_role_editor
            ListRole.VIEWER -> Res.string.grocery_role_viewer
        }
    )
