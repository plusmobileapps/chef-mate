package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

@Composable
actual fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    instanceKeeper: InstanceKeeper,
    modifier: Modifier,
) {
    val webViewState = rememberWebViewState(url = url)
    val webViewNavigator = rememberWebViewNavigator()
    var lastCommandedUrl by remember { mutableStateOf("") }

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

    WebView(state = webViewState, modifier = modifier, navigator = webViewNavigator)
}
