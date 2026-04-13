package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import java.awt.BorderLayout
import javax.swing.JPanel

@Composable
actual fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    modifier: Modifier,
) {
    val jfxPanel =
        remember {
            JFXPanel().also {
                Platform.setImplicitExit(false)
            }
        }
    val webViewRef = remember { WebViewRef() }

    DisposableEffect(Unit) {
        Platform.runLater {
            val webView = WebView()
            webViewRef.webView = webView

            webView.engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState == Worker.State.SUCCEEDED) {
                    val loadedUrl = webView.engine.location
                    if (loadedUrl != null) {
                        onUrlLoaded(loadedUrl)
                    }
                }
            }

            jfxPanel.scene = Scene(webView)

            if (url.isNotBlank()) {
                webView.engine.load(url)
            }
        }

        onDispose {
            Platform.runLater {
                webViewRef.webView?.engine?.load("about:blank")
                webViewRef.webView = null
            }
        }
    }

    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            Platform.runLater {
                val currentLocation = webViewRef.webView?.engine?.location
                if (currentLocation != url) {
                    webViewRef.webView?.engine?.load(url)
                }
            }
        }
    }

    SwingPanel(
        modifier = modifier,
        factory = {
            JPanel(BorderLayout()).apply {
                add(jfxPanel, BorderLayout.CENTER)
            }
        },
    )
}

private class WebViewRef {
    @Volatile
    var webView: WebView? = null
}
