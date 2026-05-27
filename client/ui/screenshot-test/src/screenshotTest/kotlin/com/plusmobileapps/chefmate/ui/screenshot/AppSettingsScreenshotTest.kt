package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.settings.impl.ui.previewAppSettingsBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// Regression coverage for PR #161: the App Settings screen used PlusNavContainer (no themed
// background, zeroed status-bar inset) which rendered white in dark mode and pushed the TopAppBar
// behind the system bars. Migrating to PlusHeaderContainer fixed both. These shots lock that in.

@PreviewTest
@Preview(showBackground = true)
@Composable
fun AppSettingsLightScreenshot() {
    ChefMateTheme { previewAppSettingsBloc.Content(Modifier) }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppSettingsDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewAppSettingsBloc.Content(Modifier) }
}
