package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocEmpty
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocLoading
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocLongTitle
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocSections
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocSplit
import com.plusmobileapps.chefmate.cook.impl.ui.previewCookBlocStacked
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

// ── Phone portrait (360 × 1100 dp, COMPACT width → mobile layout) ──────────

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModePhonePortraitLightScreenshot() {
    ChefMateTheme { previewCookBlocStacked.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CookModePhonePortraitDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewCookBlocStacked.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeLoadingScreenshot() {
    ChefMateTheme { previewCookBlocLoading.Content() }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeEmptyScreenshot() {
    ChefMateTheme { previewCookBlocEmpty.Content() }
}

// Long-title regression for issue #149: header must ellipsis after two lines so the floating
// app bar doesn't grow and overlap the sticky section header.
@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeLongTitleScreenshot() {
    ChefMateTheme { previewCookBlocLongTitle.Content() }
}

// Grouped ingredients (issue #177): sub-section headers ("For the …:") render bold and are not
// crossable in the ingredients list.
@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeSectionsScreenshot() {
    ChefMateTheme { previewCookBlocSections.Content() }
}

// ── Phone landscape (580 × 360 dp, COMPACT width → mobile layout, compact height) ──

@PreviewTest
@Preview(showBackground = true, widthDp = 580, heightDp = 360)
@Composable
fun CookModePhoneLandscapeLightScreenshot() {
    ChefMateTheme { previewCookBlocStacked.Content() }
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
    ChefMateTheme(darkTheme = true) { previewCookBlocStacked.Content() }
}

// ── Tablet (800 × 1100 dp, MEDIUM width → tablet layout with split body) ───

@PreviewTest
@Preview(showBackground = true, widthDp = 800, heightDp = 1100)
@Composable
fun CookModeTabletLightScreenshot() {
    ChefMateTheme { previewCookBlocSplit.Content() }
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
    ChefMateTheme(darkTheme = true) { previewCookBlocSplit.Content() }
}

// ── Tablet/large phone landscape (1000 × 460 dp, EXPANDED width, compact height) ──
// Regression coverage for issue #155: the split body must not double-pad horizontally
// when the floating header container also handles insets. Screenshot rendering doesn't
// inject real display-cutout insets, so these lock in the no-cutout baseline for the
// landscape layouts that exhibited the bug.

@PreviewTest
@Preview(showBackground = true, widthDp = 1000, heightDp = 460)
@Composable
fun CookModeSplitLandscapeLightScreenshot() {
    ChefMateTheme { previewCookBlocSplit.Content() }
}

@PreviewTest
@Preview(
    showBackground = true,
    widthDp = 1000,
    heightDp = 460,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun CookModeSplitLandscapeDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewCookBlocSplit.Content() }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 1000, heightDp = 460)
@Composable
fun CookModeStackedLandscapeWideScreenshot() {
    ChefMateTheme { previewCookBlocStacked.Content() }
}
