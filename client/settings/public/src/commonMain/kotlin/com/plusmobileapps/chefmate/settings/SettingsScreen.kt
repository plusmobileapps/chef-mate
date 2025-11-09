@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.settings

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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import chefmate.client.settings.public.generated.resources.Res
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
    PlusNavContainer(
        data =
            PlusHeaderData.Parent(
                title = Res.string.settings.asTextData(),
            ),
        content = {
            // TODO: Conditional logic to show rows once auth is implemented.
            SettingsRow(
                name = Res.string.sign_in.asTextData(),
                onClick = bloc::onSignInClicked,
            )
            HorizontalDivider()
            SettingsRow(
                name = Res.string.sign_up.asTextData(),
                onClick = bloc::onSignUpClicked,
            )
            HorizontalDivider()

            SettingsRow(
                name = Res.string.sign_out.asTextData(),
                onClick = bloc::onSignOutClicked,
            )
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

private val previewBloc =
    object : SettingsBloc {
        override fun onSignInClicked() = Unit

        override fun onSignUpClicked() = Unit

        override fun onSignOutClicked() = Unit
    }

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenPreview() {
    ChefMateTheme {
        SettingsScreen(bloc = previewBloc)
    }
}

@Preview(showBackground = true)
@Composable
internal fun SettingsScreenDarkPreview() {
    ChefMateTheme(darkTheme = true) {
        SettingsScreen(bloc = previewBloc)
    }
}
