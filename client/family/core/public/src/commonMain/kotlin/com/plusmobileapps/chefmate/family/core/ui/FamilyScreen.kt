package com.plusmobileapps.chefmate.family.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.family.core.public.generated.resources.Res
import chefmate.client.family.core.public.generated.resources.family_action_cancel
import chefmate.client.family.core.public.generated.resources.family_create_button
import chefmate.client.family.core.public.generated.resources.family_delete_button
import chefmate.client.family.core.public.generated.resources.family_delete_confirm
import chefmate.client.family.core.public.generated.resources.family_delete_message
import chefmate.client.family.core.public.generated.resources.family_delete_title
import chefmate.client.family.core.public.generated.resources.family_empty_message
import chefmate.client.family.core.public.generated.resources.family_empty_title
import chefmate.client.family.core.public.generated.resources.family_group_members
import chefmate.client.family.core.public.generated.resources.family_group_owner
import chefmate.client.family.core.public.generated.resources.family_invite_button
import chefmate.client.family.core.public.generated.resources.family_invite_email_label
import chefmate.client.family.core.public.generated.resources.family_invite_hint
import chefmate.client.family.core.public.generated.resources.family_leave_button
import chefmate.client.family.core.public.generated.resources.family_leave_confirm
import chefmate.client.family.core.public.generated.resources.family_leave_message
import chefmate.client.family.core.public.generated.resources.family_leave_title
import chefmate.client.family.core.public.generated.resources.family_member_declined
import chefmate.client.family.core.public.generated.resources.family_member_pending
import chefmate.client.family.core.public.generated.resources.family_name_label
import chefmate.client.family.core.public.generated.resources.family_remove_cancel
import chefmate.client.family.core.public.generated.resources.family_remove_confirm
import chefmate.client.family.core.public.generated.resources.family_remove_member
import chefmate.client.family.core.public.generated.resources.family_remove_message
import chefmate.client.family.core.public.generated.resources.family_remove_title
import chefmate.client.family.core.public.generated.resources.family_rename
import chefmate.client.family.core.public.generated.resources.family_rename_cancel
import chefmate.client.family.core.public.generated.resources.family_rename_confirm
import chefmate.client.family.core.public.generated.resources.family_signed_out
import chefmate.client.family.core.public.generated.resources.family_signed_out_title
import chefmate.client.family.core.public.generated.resources.family_title
import com.plusmobileapps.chefmate.family.core.FamilyBloc
import com.plusmobileapps.chefmate.family.core.FamilyTestTags
import com.plusmobileapps.chefmate.family.data.FamilyMember
import com.plusmobileapps.chefmate.family.data.FamilyMemberStatus
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
import com.plusmobileapps.chefmate.ui.components.SignedOutPrompt
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FamilyScreen(bloc: FamilyBloc, modifier: Modifier = Modifier) {
    val model by bloc.state.collectAsState()

    ConfirmationDialogs(bloc = bloc, model = model)

    PlusHeaderContainer(
        modifier = modifier.testTag(FamilyTestTags.SCREEN).imePadding(),
        data =
            PlusHeaderData.Child(
                title = Res.string.family_title.asTextData(),
                onBackClick = bloc::onBack,
            ),
        contentPadding = PaddingValues(ChefMateTheme.dimens.paddingNormal),
    ) {
        when {
            model.isLoading -> Unit
            !model.isSignedIn ->
                SignedOutPrompt(
                    title = Res.string.family_signed_out_title.asTextData(),
                    message = Res.string.family_signed_out.asTextData(),
                    onSignInClick = bloc::onSignInClicked,
                    onSignUpClick = bloc::onSignUpClicked,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier.fillMaxWidth().padding(top = ChefMateTheme.dimens.paddingLarge),
                    signInButtonModifier = Modifier.testTag(FamilyTestTags.SIGN_IN_BUTTON),
                    signUpButtonModifier = Modifier.testTag(FamilyTestTags.SIGN_UP_BUTTON),
                )
            model.family == null -> CreateFamilySection(bloc = bloc, model = model)
            else -> FamilyDetails(bloc = bloc, model = model)
        }
    }
}

@Composable
private fun CreateFamilySection(bloc: FamilyBloc, model: FamilyBloc.Model) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = ChefMateTheme.dimens.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.family_empty_title),
            style = ChefMateTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.family_empty_message),
            style = ChefMateTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        PlusTextField(
            value = model.newFamilyName,
            onValueChange = bloc::onNewFamilyNameChanged,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = ChefMateTheme.dimens.paddingNormal)
                    .testTag(FamilyTestTags.CREATE_NAME_FIELD),
            label = { Text(stringResource(Res.string.family_name_label)) },
            singleLine = true,
            error = model.createError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { bloc.onCreateFamilyClicked() }),
        )
        PlusButton(
            text = Res.string.family_create_button.asTextData(),
            isLoading = model.isCreating,
            enabled = model.canCreate,
            onClick = bloc::onCreateFamilyClicked,
            modifier = Modifier.fillMaxWidth().testTag(FamilyTestTags.CREATE_BUTTON),
        )
    }
}

@Composable
private fun FamilyDetails(bloc: FamilyBloc, model: FamilyBloc.Model) {
    val family = model.family ?: return

    if (model.editingName != null) {
        RenameField(bloc = bloc, model = model, value = model.editingName!!)
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
        ) {
            Text(
                text = family.name,
                style = ChefMateTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f).testTag(FamilyTestTags.NAME),
            )
            if (model.isOwner) {
                IconButton(
                    onClick = bloc::onRenameClicked,
                    modifier = Modifier.testTag(FamilyTestTags.RENAME_BUTTON),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.family_rename),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    MembersSection(bloc = bloc, model = model)

    DangerZone(bloc = bloc, model = model)
}

@Composable
private fun RenameField(bloc: FamilyBloc, model: FamilyBloc.Model, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall)) {
        PlusTextField(
            value = value,
            onValueChange = bloc::onEditingNameChanged,
            modifier = Modifier.fillMaxWidth().testTag(FamilyTestTags.RENAME_FIELD),
            label = { Text(stringResource(Res.string.family_name_label)) },
            singleLine = true,
            error = model.renameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { bloc.onRenameConfirmed() }),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall)) {
            PlusButton(
                text = Res.string.family_rename_cancel.asTextData(),
                variant = PlusButtonVariant.SECONDARY,
                onClick = bloc::onRenameCancelled,
                modifier = Modifier.weight(1f),
            )
            PlusButton(
                text = Res.string.family_rename_confirm.asTextData(),
                isLoading = model.isRenaming,
                enabled = model.canConfirmRename,
                onClick = bloc::onRenameConfirmed,
                modifier = Modifier.weight(1f).testTag(FamilyTestTags.RENAME_CONFIRM_BUTTON),
            )
        }
    }
}

@Composable
private fun MembersSection(bloc: FamilyBloc, model: FamilyBloc.Model) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .padding(top = ChefMateTheme.dimens.paddingLarge)
                .testTag(FamilyTestTags.MEMBERS),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        HorizontalDivider()

        // Only two groups, because a family has only two roles — the owner administers it and
        // everyone else has equal edit rights. Pending and declined invites sit in Members, dimmed.
        // No section header above these: with just "Owner" and "Members" it would only repeat the
        // group headings, unlike the recipe-book screen's three roles under "Collaborators".
        MemberGroup(
            title = Res.string.family_group_owner,
            members = model.members.filter { it.isOwner },
            canManage = model.isOwner,
            onRemove = bloc::onRemoveMemberClicked,
        )
        MemberGroup(
            title = Res.string.family_group_members,
            members = model.members.filterNot { it.isOwner },
            canManage = model.isOwner,
            onRemove = bloc::onRemoveMemberClicked,
        )

        // Invite controls are owner-only; members just see the list above.
        if (model.isOwner) {
            PlusTextField(
                value = model.inviteEmail,
                onValueChange = bloc::onInviteEmailChanged,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = ChefMateTheme.dimens.paddingSmall)
                        .testTag(FamilyTestTags.INVITE_EMAIL_FIELD),
                label = { Text(stringResource(Res.string.family_invite_email_label)) },
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
            Text(
                text = stringResource(Res.string.family_invite_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PlusButton(
                text = Res.string.family_invite_button.asTextData(),
                variant = PlusButtonVariant.SECONDARY,
                isLoading = model.isInviting,
                enabled = model.canInvite,
                onClick = bloc::onInviteClicked,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = ChefMateTheme.dimens.paddingSmall)
                        .testTag(FamilyTestTags.INVITE_BUTTON),
            )
        }
    }
}

@Composable
private fun DangerZone(bloc: FamilyBloc, model: FamilyBloc.Model) {
    // The owner deletes the family for everyone; anyone else removes only themselves. The two are
    // mutually exclusive, so only one button ever shows.
    val isDelete = model.isOwner
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = ChefMateTheme.dimens.paddingLarge),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        HorizontalDivider()
        PlusButton(
            text =
                if (isDelete) Res.string.family_delete_button.asTextData()
                else Res.string.family_leave_button.asTextData(),
            variant = PlusButtonVariant.DESTRUCTIVE,
            isLoading = model.isRemovingFamily,
            onClick = if (isDelete) bloc::onDeleteFamilyClicked else bloc::onLeaveFamilyClicked,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = ChefMateTheme.dimens.paddingSmall)
                    .testTag(
                        if (isDelete) FamilyTestTags.DELETE_BUTTON else FamilyTestTags.LEAVE_BUTTON
                    ),
        )
        model.familyActionError?.let { error ->
            Text(
                text = error.localized(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ConfirmationDialogs(bloc: FamilyBloc, model: FamilyBloc.Model) {
    model.removingMember?.let { member ->
        PlusDialog(
            title = Res.string.family_remove_title.asTextData(),
            message =
                PhraseModel(
                    resource = Res.string.family_remove_message,
                    "email" to FixedString(member.email),
                ),
            confirmButtonText = Res.string.family_remove_confirm.asTextData(),
            dismissButtonText = Res.string.family_remove_cancel.asTextData(),
            onConfirmClick = bloc::onConfirmRemoveMember,
            onDismissRequest = bloc::onDismissRemoveMember,
        )
    }

    model.pendingFamilyAction?.let { action ->
        val isDelete = action == FamilyBloc.FamilyAction.DELETE
        PlusDialog(
            title =
                if (isDelete) Res.string.family_delete_title.asTextData()
                else Res.string.family_leave_title.asTextData(),
            message =
                if (isDelete) Res.string.family_delete_message.asTextData()
                else Res.string.family_leave_message.asTextData(),
            confirmButtonText =
                if (isDelete) Res.string.family_delete_confirm.asTextData()
                else Res.string.family_leave_confirm.asTextData(),
            dismissButtonText = Res.string.family_action_cancel.asTextData(),
            onConfirmClick = bloc::onConfirmFamilyAction,
            onDismissRequest = bloc::onDismissFamilyAction,
        )
    }
}

@Composable
private fun MemberGroup(
    title: StringResource,
    members: List<FamilyMember>,
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
private fun MemberRow(member: FamilyMember, canManage: Boolean, onRemove: (String) -> Unit) {
    // name → email, with pending/declined invites dimmed and tagged. Pending invites have no
    // account
    // yet, so they fall back to the email as the primary line. A declined invite stays visible so
    // the owner can see it was turned down, and can then remove or re-invite.
    val name = member.name?.takeIf { it.isNotBlank() }
    val pending = member.status == FamilyMemberStatus.PENDING
    val declined = member.status == FamilyMemberStatus.REJECTED
    val secondary =
        listOfNotNull(
                member.email.takeIf { name != null },
                stringResource(Res.string.family_member_pending).takeIf { pending },
                stringResource(Res.string.family_member_declined).takeIf { declined },
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
        val memberId = member.id
        if (canManage && !member.isOwner && memberId != null) {
            IconButton(
                onClick = { onRemove(memberId) },
                modifier = Modifier.testTag(FamilyTestTags.REMOVE_MEMBER_PREFIX + memberId),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription =
                        stringResource(Res.string.family_remove_member, member.email),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
