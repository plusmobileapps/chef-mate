package com.plusmobileapps.chefmate.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.manage_profile_avatar_content_description
import chefmate.client.profile.public.generated.resources.manage_profile_bio_hint
import chefmate.client.profile.public.generated.resources.manage_profile_bio_label
import chefmate.client.profile.public.generated.resources.manage_profile_change_photo
import chefmate.client.profile.public.generated.resources.manage_profile_delete_account
import chefmate.client.profile.public.generated.resources.manage_profile_delete_dialog_cancel
import chefmate.client.profile.public.generated.resources.manage_profile_delete_dialog_confirm
import chefmate.client.profile.public.generated.resources.manage_profile_delete_dialog_confirmation_hint
import chefmate.client.profile.public.generated.resources.manage_profile_delete_dialog_confirmation_prompt
import chefmate.client.profile.public.generated.resources.manage_profile_delete_dialog_message
import chefmate.client.profile.public.generated.resources.manage_profile_delete_dialog_title
import chefmate.client.profile.public.generated.resources.manage_profile_display_name_hint
import chefmate.client.profile.public.generated.resources.manage_profile_display_name_label
import chefmate.client.profile.public.generated.resources.manage_profile_email_label
import chefmate.client.profile.public.generated.resources.manage_profile_handle_available
import chefmate.client.profile.public.generated.resources.manage_profile_handle_checking
import chefmate.client.profile.public.generated.resources.manage_profile_handle_help
import chefmate.client.profile.public.generated.resources.manage_profile_handle_hint
import chefmate.client.profile.public.generated.resources.manage_profile_handle_invalid
import chefmate.client.profile.public.generated.resources.manage_profile_handle_label
import chefmate.client.profile.public.generated.resources.manage_profile_handle_permanent
import chefmate.client.profile.public.generated.resources.manage_profile_handle_taken
import chefmate.client.profile.public.generated.resources.manage_profile_save
import chefmate.client.profile.public.generated.resources.manage_profile_title
import coil3.compose.AsyncImage
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusDialogScaffold
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberImagePickerLauncher

@Composable
fun ManageProfileScreen(bloc: ManageProfileBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    val pickPhoto = rememberImagePickerLauncher { picked -> picked?.let(bloc::onPhotoPicked) }

    if (state.showDeleteDialog) {
        DeleteAccountDialog(
            email = state.email,
            confirmation = state.deleteConfirmation,
            canConfirm = state.canConfirmDelete,
            onConfirmationChange = bloc::onDeleteConfirmationChanged,
            onConfirmClick = bloc::onDeleteConfirmed,
            onDismissRequest = bloc::onDeleteDismissed,
        )
    }

    PlusHeaderContainer(
        modifier = modifier.testTag(ManageProfileTestTags.SCREEN),
        data =
            PlusHeaderData.Child(
                title = Res.string.manage_profile_title.asTextData(),
                onBackClick = bloc::onBack,
            ),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal),
            ) {
                Avatar(
                    pickedPhoto = state.pickedPhoto,
                    photoUrl = state.photoUrl,
                    onClick = pickPhoto,
                )
                Text(
                    Res.string.manage_profile_change_photo.asTextData().localized(),
                    style = ChefMateTheme.typography.labelLarge,
                    color = ChefMateTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = pickPhoto),
                )

                PlusTextField(
                    value = state.displayName,
                    onValueChange = bloc::onDisplayNameChanged,
                    modifier = Modifier.fillMaxWidth().testTag(ManageProfileTestTags.DISPLAY_NAME),
                    label = {
                        Text(Res.string.manage_profile_display_name_label.asTextData().localized())
                    },
                    placeholder = {
                        Text(Res.string.manage_profile_display_name_hint.asTextData().localized())
                    },
                    singleLine = true,
                    enabled = !state.isSaving && !state.isDeleting,
                    error = state.saveError,
                )

                HandleField(
                    handle = state.handle,
                    isClaimed = state.isHandleClaimed,
                    status = state.handleStatus,
                    enabled = !state.isSaving && !state.isDeleting,
                    onValueChange = bloc::onHandleChanged,
                )

                PlusTextField(
                    value = state.bio,
                    onValueChange = bloc::onBioChanged,
                    modifier = Modifier.fillMaxWidth().testTag(ManageProfileTestTags.BIO),
                    label = { Text(Res.string.manage_profile_bio_label.asTextData().localized()) },
                    placeholder = {
                        Text(Res.string.manage_profile_bio_hint.asTextData().localized())
                    },
                    singleLine = false,
                    enabled = !state.isSaving && !state.isDeleting,
                )

                PlusTextField(
                    value = state.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(Res.string.manage_profile_email_label.asTextData().localized())
                    },
                    singleLine = true,
                    enabled = false,
                    readOnly = true,
                )

                PlusButton(
                    text = Res.string.manage_profile_save.asTextData(),
                    modifier = Modifier.fillMaxWidth().testTag(ManageProfileTestTags.SAVE),
                    enabled = state.canSave,
                    isLoading = state.isSaving,
                    onClick = bloc::onSaveClicked,
                )

                state.deleteError?.let { error ->
                    Text(
                        error.localized(),
                        style = ChefMateTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                }

                PlusButton(
                    text = Res.string.manage_profile_delete_account.asTextData(),
                    variant = PlusButtonVariant.DESTRUCTIVE,
                    modifier =
                        Modifier.fillMaxWidth().testTag(ManageProfileTestTags.DELETE_ACCOUNT),
                    enabled = !state.isSaving && !state.isDeleting,
                    isLoading = state.isDeleting,
                    onClick = bloc::onDeleteAccountClicked,
                )
            }
        },
    )
}

/**
 * The @handle field. Editable only until the handle is claimed — after that it renders read-only
 * with a note explaining why, because handles are permanent server-side and a field the user can
 * type into but never save would be a lie.
 */
@Composable
private fun HandleField(
    handle: String,
    isClaimed: Boolean,
    status: ManageProfileBloc.HandleStatus?,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
    ) {
        PlusTextField(
            value = handle,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().testTag(ManageProfileTestTags.HANDLE),
            label = { Text(Res.string.manage_profile_handle_label.asTextData().localized()) },
            placeholder = { Text(Res.string.manage_profile_handle_hint.asTextData().localized()) },
            leadingIcon = { Text("@") },
            singleLine = true,
            enabled = enabled && !isClaimed,
            readOnly = isClaimed,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
        )

        val message: TextData =
            when {
                isClaimed -> Res.string.manage_profile_handle_permanent.asTextData()
                status == ManageProfileBloc.HandleStatus.Checking ->
                    Res.string.manage_profile_handle_checking.asTextData()
                status == ManageProfileBloc.HandleStatus.Available ->
                    Res.string.manage_profile_handle_available.asTextData()
                status == ManageProfileBloc.HandleStatus.Taken ->
                    Res.string.manage_profile_handle_taken.asTextData()
                status == ManageProfileBloc.HandleStatus.InvalidFormat ->
                    Res.string.manage_profile_handle_invalid.asTextData()
                else -> Res.string.manage_profile_handle_help.asTextData()
            }
        val color: Color =
            when (status.takeUnless { isClaimed }) {
                ManageProfileBloc.HandleStatus.Available -> ChefMateTheme.colorScheme.primary
                ManageProfileBloc.HandleStatus.Taken,
                ManageProfileBloc.HandleStatus.InvalidFormat -> MaterialTheme.colorScheme.error
                else -> ChefMateTheme.colorScheme.onSurfaceVariant
            }

        Text(
            message.localized(),
            style = ChefMateTheme.typography.bodySmall,
            color = color,
            modifier = Modifier.fillMaxWidth().testTag(ManageProfileTestTags.HANDLE_STATUS),
        )
    }
}

@Composable
private fun DeleteAccountDialog(
    email: String,
    confirmation: String,
    canConfirm: Boolean,
    onConfirmationChange: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    PlusDialogScaffold(
        onDismissRequest = onDismissRequest,
        header = { Text(Res.string.manage_profile_delete_dialog_title.asTextData().localized()) },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal)) {
                Text(Res.string.manage_profile_delete_dialog_message.asTextData().localized())
                Text(
                    PhraseModel(
                            Res.string.manage_profile_delete_dialog_confirmation_prompt,
                            "email" to FixedString(email),
                        )
                        .localized()
                )
                PlusTextField(
                    value = confirmation,
                    onValueChange = onConfirmationChange,
                    modifier =
                        Modifier.fillMaxWidth().testTag(ManageProfileTestTags.DELETE_CONFIRMATION),
                    placeholder = {
                        Text(
                            Res.string.manage_profile_delete_dialog_confirmation_hint
                                .asTextData()
                                .localized()
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
            }
        },
        footer = {
            Row(horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal)) {
                PlusButton(
                    text = Res.string.manage_profile_delete_dialog_cancel.asTextData(),
                    variant = PlusButtonVariant.SECONDARY,
                    onClick = onDismissRequest,
                )
                PlusButton(
                    text = Res.string.manage_profile_delete_dialog_confirm.asTextData(),
                    variant = PlusButtonVariant.DESTRUCTIVE,
                    enabled = canConfirm,
                    modifier = Modifier.testTag(ManageProfileTestTags.DELETE_CONFIRM),
                    onClick = onConfirmClick,
                )
            }
        },
    )
}

@Composable
private fun Avatar(
    pickedPhoto: ByteArray?,
    photoUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription =
        Res.string.manage_profile_avatar_content_description.asTextData().localized()
    val avatarModifier =
        modifier
            .size(AVATAR_SIZE)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(ManageProfileTestTags.AVATAR)
    val model: Any? = pickedPhoto ?: photoUrl
    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = avatarModifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(ChefMateTheme.dimens.paddingExtraLarge),
            )
        }
    }
}

private val AVATAR_SIZE = 96.dp
