@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.browser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.instancekeeper.InstanceKeeper

@Composable
actual fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onCanNavigateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    goBackTrigger: Int,
    goForwardTrigger: Int,
    instanceKeeper: InstanceKeeper,
    modifier: Modifier,
) {
    // Placeholder for the web spike. A real implementation would render an <iframe> via
    // an HtmlView / DOM interop, subject to the target site's frame-embedding policy.
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("In-app browser is not available on web.")
    }
}
