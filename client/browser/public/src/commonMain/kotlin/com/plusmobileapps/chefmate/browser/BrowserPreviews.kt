package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun landingBloc(model: BrowserLandingBloc.Model): BrowserLandingBloc =
    object : BrowserLandingBloc {
        override val state = MutableStateFlow(model)

        override fun onSearchFieldFocused() = Unit

        override fun onCoachMarkDismissed(id: String) = Unit

        override fun onSearchEngineSelected(engine: SearchEngine) = Unit
    }

val previewBrowserLandingBloc: BrowserLandingBloc =
    landingBloc(BrowserLandingBloc.Model(selectedEngine = SearchEngine.GOOGLE))

val previewBrowserSelectEngineBloc: BrowserSelectEngineBloc =
    object : BrowserSelectEngineBloc {
        override fun onEngineSelected(engine: SearchEngine) = Unit
    }

@Preview
@Composable
internal fun BrowserLandingScreenPreview() {
    ChefMateTheme { BrowserLandingScreen(bloc = previewBrowserLandingBloc) }
}

@Preview
@Composable
internal fun BrowserSelectEngineScreenPreview() {
    ChefMateTheme { BrowserSelectEngineScreen(bloc = previewBrowserSelectEngineBloc) }
}
