@file:Suppress("UNUSED_PARAMETER")

package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator

@Composable
actual fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onCanNavigateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    goBackTrigger: Int,
    goForwardTrigger: Int,
    captureHtmlTrigger: Int,
    onHtmlCaptured: (String?) -> Unit,
    instanceKeeper: InstanceKeeper,
    modifier: Modifier,
) {
    val webViewState = rememberSaveableWebViewState(url = url)
    // The WebView library defaults its background to transparent, so a page that doesn't paint one
    // lets the app's surface show through — which reads as a black page in dark mode. Browsers
    // render on white; do the same so a site without dark styling stays legible. Sites that do
    // support dark mode paint over this. Must be set before the AndroidView factory runs.
    webViewState.webSettings.backgroundColor = Color.White
    val webViewNavigator = rememberWebViewNavigator()
    var lastCommandedUrl by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(url) {
        if (url.isNotBlank() && url != lastCommandedUrl) {
            lastCommandedUrl = url
            webViewNavigator.loadUrl(url)
        }
    }

    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let {
            lastCommandedUrl = it
            onUrlLoaded(it)
        }
    }

    LaunchedEffect(webViewState.isLoading) { onLoadingChanged(webViewState.isLoading) }

    LaunchedEffect(webViewNavigator.canGoBack, webViewNavigator.canGoForward) {
        onCanNavigateChanged(webViewNavigator.canGoBack, webViewNavigator.canGoForward)
    }

    LaunchedEffect(goBackTrigger) { if (goBackTrigger > 0) webViewNavigator.navigateBack() }

    LaunchedEffect(goForwardTrigger) {
        if (goForwardTrigger > 0) webViewNavigator.navigateForward()
    }

    LaunchedEffect(captureHtmlTrigger) {
        if (captureHtmlTrigger > 0) {
            webViewNavigator.evaluateJavaScript(CAPTURE_HTML_SCRIPT) { onHtmlCaptured(it) }
        }
    }

    WebView(state = webViewState, modifier = modifier, navigator = webViewNavigator)
}
