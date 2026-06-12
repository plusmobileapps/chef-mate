package com.plusmobileapps.chefmate.grocery.core.impl.edit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_cancel
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
import chefmate.client.grocery.core.public.generated.resources.grocery_rename_save
import chefmate.client.grocery.core.public.generated.resources.grocery_role_editor
import chefmate.client.grocery.core.public.generated.resources.grocery_role_owner
import chefmate.client.grocery.core.public.generated.resources.grocery_role_viewer
import chefmate.client.grocery.core.public.generated.resources.grocery_sign_in
import chefmate.client.grocery.core.public.generated.resources.grocery_sign_up
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListBloc
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListTestTags
import com.plusmobileapps.chefmate.grocery.data.CollaborationStatus
import com.plusmobileapps.chefmate.grocery.data.ListCollaborator
import com.plusmobileapps.chefmate.grocery.data.ListRole
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditGroceryListScreen(bloc: EditGroceryListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    val dimens = ChefMateTheme.dimens

    PlusNavContainer(
        modifier = modifier.testTag(EditGroceryListTestTags.SCREEN).fillMaxWidth(),
        data =
            PlusHeaderData.Child(
                title = Res.string.grocery_edit_list_title.asTextData(),
                onBackClick = bloc::onBackClicked,
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

        state.collaborators.forEach { collaborator ->
            CollaboratorRow(
                collaborator = collaborator,
                canRemove = state.isOwner && collaborator.role != ListRole.OWNER,
                onRemove = { bloc.onRemoveCollaborator(collaborator) },
            )
        }

        if (state.isOwner) {
            InviteRow(onInvite = bloc::onInviteCollaborator)
        }
    }
}

@Composable
private fun CollaboratorRow(
    collaborator: ListCollaborator,
    canRemove: Boolean,
    onRemove: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(collaborator.displayName ?: collaborator.email) },
        supportingContent = {
            val roleText =
                when (collaborator.role) {
                    ListRole.OWNER -> stringResource(Res.string.grocery_role_owner)
                    ListRole.EDITOR -> stringResource(Res.string.grocery_role_editor)
                    ListRole.VIEWER -> stringResource(Res.string.grocery_role_viewer)
                }
            val statusText =
                if (collaborator.status == CollaborationStatus.PENDING) " (pending)" else ""
            Text("$roleText$statusText")
        },
        trailingContent =
            if (canRemove) {
                {
                    IconButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.PersonRemove,
                            contentDescription =
                                stringResource(Res.string.grocery_remove_collaborator),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else {
                null
            },
    )
}

@Composable
private fun InviteRow(onInvite: (String) -> Unit) {
    val dimens = ChefMateTheme.dimens
    var inviteEmail by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        PlusTextField(
            value = inviteEmail,
            onValueChange = { inviteEmail = it },
            label = { Text(stringResource(Res.string.grocery_invite_hint)) },
            singleLine = true,
            modifier = Modifier.weight(1f).testTag(EditGroceryListTestTags.INVITE_FIELD),
        )
        IconButton(
            onClick = {
                if (inviteEmail.isNotBlank()) {
                    onInvite(inviteEmail)
                    inviteEmail = ""
                }
            },
            enabled = inviteEmail.isNotBlank(),
            modifier = Modifier.testTag(EditGroceryListTestTags.INVITE_BUTTON),
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = stringResource(Res.string.grocery_invite),
            )
        }
    }
}
