package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import java.awt.BorderLayout
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.JPanel

@Composable
actual fun PlatformWebView(
    url: String,
    onUrlLoaded: (String) -> Unit,
    instanceKeeper: InstanceKeeper,
    modifier: Modifier,
) {
    val holder = remember { instanceKeeper.getOrCreate { WebViewHolder() } }

    DisposableEffect(Unit) {
        holder.ensureInitialized(onUrlLoaded, url)
        onDispose {}
    }

    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            Platform.runLater {
                val currentLocation = holder.webView?.engine?.location
                if (currentLocation != url) {
                    holder.webView?.engine?.load(url)
                }
            }
        }
    }

    SwingPanel(
        modifier = modifier,
        factory = { JPanel(BorderLayout()).apply { add(holder.jfxPanel, BorderLayout.CENTER) } },
    )
}

private class WebViewHolder : InstanceKeeper.Instance {
    val jfxPanel: JFXPanel = JFXPanel().also { Platform.setImplicitExit(false) }

    @Volatile
    var webView: WebView? = null
        private set

    private var initialized = false

    fun ensureInitialized(onUrlLoaded: (String) -> Unit, initialUrl: String) {
        if (initialized) return
        initialized = true

        Platform.runLater {
            val wv = WebView()
            webView = wv

            wv.engine.loadWorker.stateProperty().addListener { _, _, newState ->
                if (newState == Worker.State.SUCCEEDED) {
                    wv.engine.location?.let(onUrlLoaded)
                }
            }

            jfxPanel.scene = Scene(wv)

            if (initialUrl.isNotBlank()) {
                wv.engine.load(initialUrl)
            }
        }
    }

    override fun onDestroy() {
        Platform.runLater {
            webView?.engine?.load("about:blank")
            webView = null
        }
    }
}
