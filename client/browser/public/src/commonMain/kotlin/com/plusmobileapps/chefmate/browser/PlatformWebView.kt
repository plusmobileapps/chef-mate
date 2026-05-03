package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

/**
 * Platform-specific WebView composable. On mobile platforms this renders a native in-app browser.
 * On desktop it provides a fallback experience.
 *
 * @param url The URL to load in the browser.
 * @param onUrlLoaded Callback invoked when the WebView finishes navigating to a URL.
 * @param onLoadingChanged Callback invoked when the WebView loading state changes.
 * @param onCanNavigateChanged Callback invoked when back/forward availability changes.
 * @param goBackTrigger Incremented each time the user requests to navigate back.
 * @param goForwardTrigger Incremented each time the user requests to navigate forward.
 * @param instanceKeeper Used on JVM to cache the WebView across recompositions so browser history
 *   survives tab switches.
 * @param modifier Modifier for layout.
 */
@Composable
expect fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onCanNavigateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    goBackTrigger: Int,
    goForwardTrigger: Int,
    instanceKeeper: InstanceKeeper,
    modifier: Modifier = Modifier,
)
