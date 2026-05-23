package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.settings.root.SettingsRootScreen
import com.plusmobileapps.chefmate.settings.root.previewSettingsRootMasterOnly
import com.plusmobileapps.chefmate.settings.root.previewSettingsRootWithDetails
import com.plusmobileapps.chefmate.settings.root.previewSettingsRootWithDirtyDetails
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// ── Phone portrait (COMPACT width → single-pane stacking) ────────────────────

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsRootCompactMasterLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootMasterOnly) }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsRootCompactMasterDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { SettingsRootScreen(bloc = previewSettingsRootMasterOnly) }
}

// In COMPACT, having a details child active causes the details pane to take over the whole screen.
@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsRootCompactDetailLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootWithDetails) }
}

// ── Tablet (EXPANDED width → dual-pane layout) ───────────────────────────────

// Master only on tablet renders with the list centered horizontally.
@PreviewTest
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
fun SettingsRootTabletMasterOnlyLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootMasterOnly) }
}

@PreviewTest
@Preview(
    showBackground = true,
    widthDp = 1000,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SettingsRootTabletMasterOnlyDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { SettingsRootScreen(bloc = previewSettingsRootMasterOnly) }
}

// Master + detail on tablet — the list is on the left, BottomNavOrder on the right.
@PreviewTest
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
fun SettingsRootTabletWithDetailsLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootWithDetails) }
}

@PreviewTest
@Preview(
    showBackground = true,
    widthDp = 1000,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun SettingsRootTabletWithDetailsDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { SettingsRootScreen(bloc = previewSettingsRootWithDetails) }
}

// Dirty detail state — locks in the BottomNavOrder Save FAB rendering inside the detail pane.
@PreviewTest
@Preview(showBackground = true, widthDp = 1000, heightDp = 800)
@Composable
fun SettingsRootTabletWithDirtyDetailsLightScreenshot() {
    ChefMateTheme { SettingsRootScreen(bloc = previewSettingsRootWithDirtyDetails) }
}
