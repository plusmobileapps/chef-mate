package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

@Composable
actual fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    modifier: Modifier,
) {
    val webViewState = rememberWebViewState(url = url)
    val webViewNavigator = rememberWebViewNavigator()

    LaunchedEffect(url) {
        if (url.isNotBlank() && url != webViewState.lastLoadedUrl) {
            webViewNavigator.loadUrl(url)
        }
    }

    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let(onUrlLoaded)
    }

    WebView(
        state = webViewState,
        modifier = modifier,
        navigator = webViewNavigator,
    )
}
