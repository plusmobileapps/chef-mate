package com.plusmobileapps.chefmate.ui.screenshot

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.plusmobileapps.chefmate.browser.previewBrowserLandingBloc
import com.plusmobileapps.chefmate.browser.previewBrowserSelectEngineBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun BrowserSelectEngineLightScreenshot() {
    ChefMateTheme { previewBrowserSelectEngineBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BrowserSelectEngineDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewBrowserSelectEngineBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun BrowserLandingLightScreenshot() {
    ChefMateTheme { previewBrowserLandingBloc.Content() }
}

@PreviewTest
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BrowserLandingDarkScreenshot() {
    ChefMateTheme(darkTheme = true) { previewBrowserLandingBloc.Content() }
}
