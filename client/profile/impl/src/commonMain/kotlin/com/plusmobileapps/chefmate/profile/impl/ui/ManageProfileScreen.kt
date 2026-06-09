package com.plusmobileapps.chefmate.profile.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import chefmate.client.profile.public.generated.resources.Res
import chefmate.client.profile.public.generated.resources.manage_profile_display_name_hint
import chefmate.client.profile.public.generated.resources.manage_profile_display_name_label
import chefmate.client.profile.public.generated.resources.manage_profile_email_label
import chefmate.client.profile.public.generated.resources.manage_profile_save
import chefmate.client.profile.public.generated.resources.manage_profile_title
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.profile.ManageProfileTestTags
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun ManageProfileScreen(bloc: ManageProfileBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

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
