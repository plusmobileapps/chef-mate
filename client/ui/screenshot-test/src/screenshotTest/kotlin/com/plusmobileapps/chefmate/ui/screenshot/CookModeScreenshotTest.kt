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

@PreviewTest
@Preview(showBackground = true, heightDp = 1100)
@Composable
fun CookModeStackedScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocStacked) }
}

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, widthDp = 800)
@Composable
fun CookModeSplitScreenshot() {
    ChefMateTheme { CookModeScreen(bloc = previewCookBlocSplit) }
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

@PreviewTest
@Preview(showBackground = true, heightDp = 1100, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CookModeStackedDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { CookModeScreen(bloc = previewCookBlocStacked) }
}

@PreviewTest
@Preview(
    showBackground = true,
    heightDp = 1100,
    widthDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun CookModeSplitDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { CookModeScreen(bloc = previewCookBlocSplit) }
}

// Mobile landscape: widthDp stays under 600dp so the responsive container picks the
// COMPACT (mobile) shell with the bottom-sheet peek row, in landscape orientation.
@PreviewTest
@Preview(showBackground = true, widthDp = 580, heightDp = 360)
@Composable
fun CookModeStackedLandscapeScreenshot() {
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
fun CookModeStackedLandscapeDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { CookModeScreen(bloc = previewCookBlocStacked) }
}
