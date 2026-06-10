package com.plusmobileapps.chefmate.profile.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.manage_profile_avatar_content_description
import chefmate.client.profile.public.generated.resources.manage_profile_change_photo
import chefmate.client.profile.public.generated.resources.manage_profile_display_name_hint
import chefmate.client.profile.public.generated.resources.manage_profile_display_name_label
import chefmate.client.profile.public.generated.resources.manage_profile_email_label
import chefmate.client.profile.public.generated.resources.manage_profile_save
import chefmate.client.profile.public.generated.resources.manage_profile_title
import coil3.compose.AsyncImage
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.profile.ManageProfileTestTags
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberImagePickerLauncher

@Composable
fun ManageProfileScreen(bloc: ManageProfileBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    val pickPhoto = rememberImagePickerLauncher { picked -> picked?.let(bloc::onPhotoPicked) }

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
                    enabled = !state.isSaving,
                    error = state.saveError,
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
