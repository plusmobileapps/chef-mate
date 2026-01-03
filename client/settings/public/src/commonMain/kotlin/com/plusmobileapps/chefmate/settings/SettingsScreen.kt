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
import chefmate.client.settings.public.generated.resources.Res
import chefmate.client.settings.public.generated.resources.greeting_authenticated
import chefmate.client.settings.public.generated.resources.settings
import chefmate.client.settings.public.generated.resources.sign_in
import chefmate.client.settings.public.generated.resources.sign_out
import chefmate.client.settings.public.generated.resources.sign_up
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsScreen(
    bloc: SettingsBloc,
    modifier: Modifier = Modifier,
) {
    val viewState by bloc.state.collectAsState()

    PlusNavContainer(
        data =
            PlusHeaderData.Parent(
                title = Res.string.settings.asTextData(),
            ),
        content = {
            if (viewState.isAuthenticated) {
                // Show greeting and sign out button when authenticated
                viewState.greeting?.let { greeting ->
                    GreetingSection(greeting = greeting)
                    HorizontalDivider()
                }
                SettingsRow(
                    name = Res.string.sign_out.asTextData(),
                    onClick = bloc::onSignOutClicked,
                )
            } else {
                // Show email verification message if present
                viewState.verificationMessage?.let { message ->
                    EmailVerificationMessage(message = message)
                    HorizontalDivider()
                }
                // Show sign in/sign up buttons when not authenticated
                SettingsRow(
                    name = Res.string.sign_in.asTextData(),
                    onClick = bloc::onSignInClicked,
                )
                HorizontalDivider()
                SettingsRow(
                    name = Res.string.sign_up.asTextData(),
                    onClick = bloc::onSignUpClicked,
                )
            }
        },
    )
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text(
                    Res.string.settings
                        .asTextData()
                        .localized(),
                )
            },
            windowInsets = WindowInsets(),
        )
    }
}

@Composable
private fun GreetingSection(
    greeting: TextData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChefMateTheme.dimens.rowHeight)
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            greeting.localized(),
            style = ChefMateTheme.typography.headlineSmall,
        )
    }
}

@Composable
private fun EmailVerificationMessage(
    message: TextData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(ChefMateTheme.dimens.paddingNormal),
    ) {
        Text(
            message.localized(),
            style = ChefMateTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun SettingsRow(
    name: TextData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentDescription = name.localized()
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChefMateTheme.dimens.rowHeight)
                .clickable { onClick() }
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal)
                .semantics {
                    this.contentDescription = contentDescription
                },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name.localized(),
            style = ChefMateTheme.typography.titleMedium,
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

private val previewBlocUnauthenticated =
    object : SettingsBloc {
        override val state =
            kotlinx.coroutines.flow.MutableStateFlow(
                SettingsBloc.Model(
                    isAuthenticated = false,
                    greeting = null,
                ),
            )

        override fun onSignInClicked() = Unit

        override fun onSignUpClicked() = Unit

        override fun onSignOutClicked() = Unit
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
                            "name" to
                                com.plusmobileapps.chefmate.text
                                    .FixedString("John Doe"),
                        ),
                ),
            )

        override fun onSignInClicked() = Unit

        override fun onSignUpClicked() = Unit

        override fun onSignOutClicked() = Unit
    }

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenUnauthenticatedPreview() {
    ChefMateTheme {
        SettingsScreen(bloc = previewBlocUnauthenticated)
    }
}

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenAuthenticatedPreview() {
    ChefMateTheme {
        SettingsScreen(bloc = previewBlocAuthenticated)
    }
}

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenDarkPreview() {
    ChefMateTheme(darkTheme = true) {
        SettingsScreen(bloc = previewBlocUnauthenticated)
    }
}
