package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific WebView composable. On mobile platforms this renders
 * a native in-app browser. On desktop it provides a fallback experience.
 *
 * @param url The URL to load in the browser.
 * @param onUrlLoaded Callback invoked when the WebView finishes navigating to a URL.
 * @param modifier Modifier for layout.
 */
@Composable
expect fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    modifier: Modifier = Modifier,
)
