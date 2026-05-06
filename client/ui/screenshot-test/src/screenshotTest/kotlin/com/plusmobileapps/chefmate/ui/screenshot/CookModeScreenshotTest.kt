package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.cook.CookModeScreen
import com.plusmobileapps.chefmate.cook.previewCookBlocEmpty
import com.plusmobileapps.chefmate.cook.previewCookBlocLoading
import com.plusmobileapps.chefmate.cook.previewCookBlocSplit
import com.plusmobileapps.chefmate.cook.previewCookBlocStacked
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// ── Phone portrait (360 × 1100 dp, COMPACT width → mobile layout) ──────────

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModePhonePortraitLightScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocStacked) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CookModePhonePortraitDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { CookModeScreen(bloc = previewCookBlocStacked) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeLoadingScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocLoading) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeEmptyScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocEmpty) }
}

// ── Phone landscape (580 × 360 dp, COMPACT width → mobile layout, compact height) ──

@PreviewTest
@Preview(showBackground = true, widthDp = 580, heightDp = 360)
@Composable
fun CookModePhoneLandscapeLightScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocStacked) }
}

@PreviewTest
@Preview(
    showBackground = true,
    widthDp = 580,
    heightDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun CookModePhoneLandscapeDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { CookModeScreen(bloc = previewCookBlocStacked) }
}

// ── Tablet (800 × 1100 dp, MEDIUM width → tablet layout with split body) ───

@PreviewTest
@Preview(showBackground = true, widthDp = 800, heightDp = 1100)
@Composable
fun CookModeTabletLightScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocSplit) }
}

@PreviewTest
@Preview(
    showBackground = true,
    widthDp = 800,
    heightDp = 1100,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun CookModeTabletDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { CookModeScreen(bloc = previewCookBlocSplit) }
}
