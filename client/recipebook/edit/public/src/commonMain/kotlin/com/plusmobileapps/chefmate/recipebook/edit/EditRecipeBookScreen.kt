@file:OptIn(ExperimentalLayoutApi::class)

package com.plusmobileapps.chefmate.recipebook.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import chefmate.client.recipebook.edit.public.generated.resources.Res
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_collaborators
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_group_editors
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_group_owner
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_group_viewers
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_invite_button
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_invite_email_label
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_member_declined
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_member_pending
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_name_label
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_remove_cancel
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_remove_confirm
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_remove_member
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_remove_message
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_remove_title
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_role_editor
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_role_owner
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_role_viewer
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_save
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMember
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookMemberStatus
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRole
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
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
fun EditRecipeBookScreen(bloc: EditRecipeBookBloc, modifier: Modifier = Modifier) {
    val model by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier.testTag(EditRecipeBookTestTags.SCREEN).imePadding(),
        data = PlusHeaderData.Modal(title = model.title, onCloseClick = bloc::onCloseClicked),
        contentPadding = PaddingValues(horizontal = ChefMateTheme.dimens.paddingNormal),
    ) {
        PlusTextField(
            value = model.name,
            onValueChange = bloc::onNameChanged,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = ChefMateTheme.dimens.paddingNormal)
                    .testTag(EditRecipeBookTestTags.NAME_FIELD),
            label = { Text(stringResource(Res.string.edit_recipe_book_name_label)) },
            singleLine = true,
            error = model.nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { bloc.onSaveClicked() }),
        )

        PlusButton(
            text = Res.string.edit_recipe_book_save.asTextData(),
            isLoading = model.isSaving,
            enabled = model.canSave,
            onClick = bloc::onSaveClicked,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = ChefMateTheme.dimens.paddingNormal)
                    .testTag(EditRecipeBookTestTags.SAVE_BUTTON),
        )

        if (model.members.isNotEmpty()) {
            CollaboratorsSection(bloc = bloc, model = model)
        }
    }

    model.removingMember?.let { member ->
        PlusDialog(
            title = Res.string.edit_recipe_book_remove_title.asTextData(),
            message =
                PhraseModel(
                    Res.string.edit_recipe_book_remove_message,
                    "name" to FixedString(member.name?.takeIf { it.isNotBlank() } ?: member.email),
                ),
            confirmButtonText = Res.string.edit_recipe_book_remove_confirm.asTextData(),
            dismissButtonText = Res.string.edit_recipe_book_remove_cancel.asTextData(),
            onConfirmClick = bloc::onConfirmRemoveMember,
            onDismissRequest = bloc::onDismissRemoveMember,
        )
    }
}

@Composable
private fun CollaboratorsSection(bloc: EditRecipeBookBloc, model: EditRecipeBookBloc.Model) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = ChefMateTheme.dimens.paddingLarge)
                .testTag(EditRecipeBookTestTags.COLLABORATORS),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        HorizontalDivider()
        Text(
            text = stringResource(Res.string.edit_recipe_book_collaborators),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = ChefMateTheme.dimens.paddingSmall),
        )

        // Grouped by role so the owner is separated from editors and viewers. Pending invites keep
        // their invited role, so they land in the matching group (dimmed).
        MemberGroup(
            title = Res.string.edit_recipe_book_group_owner,
            members = model.members.filter { it.isOwner || it.role == RecipeBookRole.OWNER },
            canManage = model.canManageCollaborators,
            onRemove = bloc::onRemoveMemberClicked,
        )
        MemberGroup(
            title = Res.string.edit_recipe_book_group_editors,
            members = model.members.filter { !it.isOwner && it.role == RecipeBookRole.EDITOR },
            canManage = model.canManageCollaborators,
            onRemove = bloc::onRemoveMemberClicked,
        )
        MemberGroup(
            title = Res.string.edit_recipe_book_group_viewers,
            members = model.members.filter { !it.isOwner && it.role == RecipeBookRole.VIEWER },
            canManage = model.canManageCollaborators,
            onRemove = bloc::onRemoveMemberClicked,
        )

        // Invite controls are owner-only; collaborators just see the list above.
        if (model.canManageCollaborators) {
            PlusTextField(
                value = model.inviteEmail,
                onValueChange = bloc::onInviteEmailChanged,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = ChefMateTheme.dimens.paddingSmall)
                        .testTag(EditRecipeBookTestTags.INVITE_EMAIL_FIELD),
                label = { Text(stringResource(Res.string.edit_recipe_book_invite_email_label)) },
                singleLine = true,
                error = model.inviteError,
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
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall)
            ) {
                listOf(RecipeBookRole.EDITOR, RecipeBookRole.VIEWER).forEach { role ->
                    FilterChip(
                        selected = model.inviteRole == role,
                        onClick = { bloc.onInviteRoleChanged(role) },
                        label = { Text(role.label()) },
                    )
                }
            }

            PlusButton(
                text = Res.string.edit_recipe_book_invite_button.asTextData(),
                variant = PlusButtonVariant.SECONDARY,
                isLoading = model.isInviting,
                enabled = model.inviteEmail.isNotBlank() && !model.isInviting,
                onClick = bloc::onInviteClicked,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = ChefMateTheme.dimens.paddingSmall)
                        .testTag(EditRecipeBookTestTags.INVITE_BUTTON),
            )
        }
    }
}

@Composable
private fun MemberGroup(
    title: StringResource,
    members: List<RecipeBookMember>,
    canManage: Boolean,
    onRemove: (String) -> Unit,
) {
    if (members.isEmpty()) return
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = ChefMateTheme.dimens.paddingSmall),
    )
    members.forEach { member ->
        MemberRow(member = member, canManage = canManage, onRemove = onRemove)
    }
}

@Composable
private fun MemberRow(member: RecipeBookMember, canManage: Boolean, onRemove: (String) -> Unit) {
    // name → email, with pending/declined invites dimmed and tagged. Role is conveyed by the group
    // header above, so it isn't repeated per row. Pending invites have no account, so they fall
    // back
    // to the email as the primary line. A declined invite stays visible so the owner can see the
    // recipient turned it down (and can then remove or re-invite them).
    val name = member.name?.takeIf { it.isNotBlank() }
    val pending = member.status == RecipeBookMemberStatus.PENDING
    val declined = member.status == RecipeBookMemberStatus.REJECTED
    val secondary =
        listOfNotNull(
                member.email.takeIf { name != null },
                stringResource(Res.string.edit_recipe_book_member_pending).takeIf { pending },
                stringResource(Res.string.edit_recipe_book_member_declined).takeIf { declined },
            )
            .joinToString(" · ")
    Row(
        modifier = Modifier.fillMaxWidth().alpha(if (pending || declined) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        PlusAvatar(
            imageUrl = member.avatarUrl,
            contentDescription = null,
            fallbackText = name ?: member.email,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name ?: member.email, style = MaterialTheme.typography.bodyMedium)
            if (secondary.isNotEmpty()) {
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (canManage && !member.isOwner && member.id != null) {
            IconButton(onClick = { onRemove(member.id!!) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription =
                        stringResource(Res.string.edit_recipe_book_remove_member, member.email),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun RecipeBookRole.label(): String =
    stringResource(
        when (this) {
            RecipeBookRole.OWNER -> Res.string.edit_recipe_book_role_owner
            RecipeBookRole.EDITOR -> Res.string.edit_recipe_book_role_editor
            RecipeBookRole.VIEWER -> Res.string.edit_recipe_book_role_viewer
        }
    )
