@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

private const val KEY_WINDOW_WIDTH = "window.width"
private const val KEY_WINDOW_HEIGHT = "window.height"
private const val KEY_WINDOW_X = "window.x"
private const val KEY_WINDOW_Y = "window.y"
private const val KEY_WINDOW_PLACEMENT = "window.placement"

@OptIn(ExperimentalTime::class, FlowPreview::class)
fun main() {
    // Initialize Bugsnag + Kermit logging for JVM
    BugsnagInitializer().initialize(BuildConfig.BUGSNAG_API_KEY)
    // Only initialize the lifecycle outside the application block
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()
    val appComponent = dev.zacsweers.metro.createGraph<JvmApplicationComponent>()

    val settings = appComponent.settings
    val initialSize =
        DpSize(
            settings.getFloatOrNull(KEY_WINDOW_WIDTH)?.dp ?: 1024.dp,
            settings.getFloatOrNull(KEY_WINDOW_HEIGHT)?.dp ?: 768.dp,
        )
    val savedX = settings.getFloatOrNull(KEY_WINDOW_X)
    val savedY = settings.getFloatOrNull(KEY_WINDOW_Y)
    val initialPosition =
        if (savedX != null && savedY != null) {
            WindowPosition.Absolute(savedX.dp, savedY.dp)
        } else {
            WindowPosition.PlatformDefault
        }
    val initialPlacement =
        when (settings.getStringOrNull(KEY_WINDOW_PLACEMENT)) {
            WindowPlacement.Maximized.name -> WindowPlacement.Maximized
            WindowPlacement.Fullscreen.name -> WindowPlacement.Fullscreen
            else -> WindowPlacement.Floating
        }

    application {
        // Initialize the DefaultComponentContext inside the application block
        // to ensure it runs on the main thread
        val rootBloc =
            buildRoot(
                componentContext =
                    DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher),
                applicationComponent = appComponent,
            )

        val windowState =
            rememberWindowState(
                placement = initialPlacement,
                size = initialSize,
                position = initialPosition,
            )

        LaunchedEffect(windowState) {
            snapshotFlow {
                    WindowSnapshot(
                        placement = windowState.placement,
                        size = windowState.size,
                        position = windowState.position,
                    )
                }
                .debounce(250)
                .collect { snapshot ->
                    settings.putString(KEY_WINDOW_PLACEMENT, snapshot.placement.name)
                    if (snapshot.placement == WindowPlacement.Floating) {
                        settings.putFloat(KEY_WINDOW_WIDTH, snapshot.size.width.value)
                        settings.putFloat(KEY_WINDOW_HEIGHT, snapshot.size.height.value)
                        val position = snapshot.position
                        if (position is WindowPosition.Absolute) {
                            settings.putFloat(KEY_WINDOW_X, position.x.value)
                            settings.putFloat(KEY_WINDOW_Y, position.y.value)
                        }
                    }
                }
        }

        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Chef Mate",
            icon = painterResource("app-icon.png"),
            onKeyEvent = { event ->
                if ((event.key == Key.Escape) && (event.type == KeyEventType.KeyUp)) {
                    backDispatcher.back()
                } else {
                    false
                }
            },
        ) {
            App(rootBloc = rootBloc)
        }
    }
}

private data class WindowSnapshot(
    val placement: WindowPlacement,
    val size: DpSize,
    val position: WindowPosition,
)
