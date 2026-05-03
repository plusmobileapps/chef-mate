@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import chefmate.client.settings.public.generated.resources.Res
import chefmate.client.settings.public.generated.resources.about
import chefmate.client.settings.public.generated.resources.greeting_authenticated
import chefmate.client.settings.public.generated.resources.more
import chefmate.client.settings.public.generated.resources.privacy_policy
import chefmate.client.settings.public.generated.resources.sign_in
import chefmate.client.settings.public.generated.resources.sign_out
import chefmate.client.settings.public.generated.resources.sign_out_confirmation_cancel
import chefmate.client.settings.public.generated.resources.sign_out_confirmation_confirm
import chefmate.client.settings.public.generated.resources.sign_out_confirmation_message
import chefmate.client.settings.public.generated.resources.sign_out_confirmation_title
import chefmate.client.settings.public.generated.resources.sign_up
import chefmate.client.settings.public.generated.resources.terms_of_use
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun SettingsScreen(bloc: SettingsBloc, modifier: Modifier = Modifier) {
    val viewState by bloc.state.collectAsState()

    if (viewState.showSignOutConfirmationDialog) {
        PlusDialog(
            title = Res.string.sign_out_confirmation_title.asTextData(),
            message = Res.string.sign_out_confirmation_message.asTextData(),
            confirmButtonText = Res.string.sign_out_confirmation_confirm.asTextData(),
            dismissButtonText = Res.string.sign_out_confirmation_cancel.asTextData(),
            onConfirmClick = bloc::onSignOutConfirmed,
            onDismissRequest = bloc::onSignOutDismissed,
        )
    }

    PlusNavContainer(
        data = PlusHeaderData.Parent(title = Res.string.more.asTextData()),
        content = {
            if (viewState.isAuthenticated) {
                viewState.greeting?.let { greeting ->
                    GreetingSection(greeting = greeting)
                    HorizontalDivider()
                }
                SettingsRow(
                    name = Res.string.sign_out.asTextData(),
                    onClick = bloc::onSignOutClicked,
                )
            } else {
                viewState.verificationMessage?.let { message ->
                    EmailVerificationMessage(message = message)
                    HorizontalDivider()
                }
                SettingsRow(name = Res.string.sign_in.asTextData(), onClick = bloc::onSignInClicked)
                HorizontalDivider()
                SettingsRow(name = Res.string.sign_up.asTextData(), onClick = bloc::onSignUpClicked)
            }
            HorizontalDivider()
            SettingsRow(
                name = Res.string.privacy_policy.asTextData(),
                onClick = {
                    bloc.onUrlClicked("https://chefmate.plusmobileapps.com/privacy-policy/")
                },
            )
            HorizontalDivider()
            SettingsRow(
                name = Res.string.terms_of_use.asTextData(),
                onClick = { bloc.onUrlClicked("https://chefmate.plusmobileapps.com/terms-of-use/") },
            )
            HorizontalDivider()
            SettingsRow(
                name = Res.string.about.asTextData(),
                onClick = { bloc.onUrlClicked("https://chefmate.plusmobileapps.com/") },
            )
        },
    )
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(Res.string.more.asTextData().localized()) },
            windowInsets = WindowInsets(),
        )
    }
}

@Composable
private fun GreetingSection(greeting: TextData, modifier: Modifier = Modifier) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChefMateTheme.dimens.rowHeight)
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(greeting.localized(), style = ChefMateTheme.typography.headlineSmall)
    }
}

@Composable
private fun EmailVerificationMessage(message: TextData, modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(ChefMateTheme.dimens.paddingNormal)
    ) {
        Text(
            message.localized(),
            style = ChefMateTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
internal fun SettingsRow(name: TextData, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentDescription = name.localized()
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChefMateTheme.dimens.rowHeight)
                .clickable { onClick() }
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name.localized(), style = ChefMateTheme.typography.titleMedium)
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

private val previewBlocUnauthenticated =
    object : SettingsBloc {
        override val state =
            kotlinx.coroutines.flow.MutableStateFlow(
                SettingsBloc.Model(isAuthenticated = false, greeting = null)
            )

        override fun onSignInClicked() = Unit

        override fun onSignUpClicked() = Unit

        override fun onSignOutClicked() = Unit

        override fun onSignOutConfirmed() = Unit

        override fun onSignOutDismissed() = Unit

        override fun onUrlClicked(url: String) = Unit
    }

private val previewBlocAuthenticated =
    object : SettingsBloc {
        override val state =
            kotlinx.coroutines.flow.MutableStateFlow(
                SettingsBloc.Model(
                    isAuthenticated = true,
                    greeting =
                        com.plusmobileapps.chefmate.text.PhraseModel(
                            resource = Res.string.greeting_authenticated,
                            "name" to com.plusmobileapps.chefmate.text.FixedString("John Doe"),
                        ),
                )
            )

        override fun onSignInClicked() = Unit

        override fun onSignUpClicked() = Unit

        override fun onSignOutClicked() = Unit

        override fun onSignOutConfirmed() = Unit

        override fun onSignOutDismissed() = Unit

        override fun onUrlClicked(url: String) = Unit
    }

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenUnauthenticatedPreview() {
    ChefMateTheme { SettingsScreen(bloc = previewBlocUnauthenticated) }
}

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenAuthenticatedPreview() {
    ChefMateTheme { SettingsScreen(bloc = previewBlocAuthenticated) }
}

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenDarkPreview() {
    ChefMateTheme(darkTheme = true) { SettingsScreen(bloc = previewBlocUnauthenticated) }
}
