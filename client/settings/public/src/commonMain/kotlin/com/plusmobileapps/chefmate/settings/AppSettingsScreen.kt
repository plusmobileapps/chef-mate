package com.plusmobileapps.chefmate.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import chefmate.client.settings.public.generated.resources.Res
import chefmate.client.settings.public.generated.resources.app_settings_bottom_nav_order
import chefmate.client.settings.public.generated.resources.app_settings_browser_history_enabled
import chefmate.client.settings.public.generated.resources.app_settings_browser_section
import chefmate.client.settings.public.generated.resources.app_settings_clear_history
import chefmate.client.settings.public.generated.resources.app_settings_clear_history_dialog_cancel
import chefmate.client.settings.public.generated.resources.app_settings_clear_history_dialog_confirm
import chefmate.client.settings.public.generated.resources.app_settings_clear_history_dialog_message
import chefmate.client.settings.public.generated.resources.app_settings_clear_history_dialog_title
import chefmate.client.settings.public.generated.resources.app_settings_navigation_section
import chefmate.client.settings.public.generated.resources.app_settings_title
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun AppSettingsScreen(bloc: AppSettingsBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    if (state.showClearHistoryDialog) {
        ClearHistoryDialog(
            onConfirm = bloc::onClearHistoryConfirmed,
            onDismiss = bloc::onClearHistoryDismissed,
        )
    }

    PlusHeaderContainer(
        modifier = modifier,
        data =
            PlusHeaderData.Child(
                title = Res.string.app_settings_title.asTextData(),
                onBackClick = bloc::onBack,
            ),
        content = {
            SectionHeader(name = Res.string.app_settings_navigation_section.asTextData())
            SettingsRow(
                name = Res.string.app_settings_bottom_nav_order.asTextData(),
                onClick = bloc::onBottomNavOrderClicked,
            )
            HorizontalDivider()
            SectionHeader(name = Res.string.app_settings_browser_section.asTextData())
            HistoryToggleRow(
                name = Res.string.app_settings_browser_history_enabled.asTextData(),
                checked = state.isHistoryEnabled,
                onCheckedChange = bloc::onHistoryEnabledChanged,
            )
            HorizontalDivider()
            SettingsRow(
                name = Res.string.app_settings_clear_history.asTextData(),
                onClick = bloc::onClearHistoryClicked,
            )
        },
    )
}

@Composable
private fun ClearHistoryDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    PlusDialog(
        title = Res.string.app_settings_clear_history_dialog_title.asTextData(),
        message = Res.string.app_settings_clear_history_dialog_message.asTextData(),
        confirmButtonText = Res.string.app_settings_clear_history_dialog_confirm.asTextData(),
        dismissButtonText = Res.string.app_settings_clear_history_dialog_cancel.asTextData(),
        onConfirmClick = onConfirm,
        onDismissRequest = onDismiss,
    )
}

@Composable
private fun SectionHeader(name: TextData, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name.localized(),
            style = ChefMateTheme.typography.titleSmall,
            color = ChefMateTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = ChefMateTheme.dimens.paddingSmall),
        )
    }
}

@Composable
private fun HistoryToggleRow(
    name: TextData,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ChefMateTheme.dimens.rowHeight)
                .clickable { onCheckedChange(!checked) }
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name.localized(), style = ChefMateTheme.typography.titleMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
