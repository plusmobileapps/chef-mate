@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.plusmobileapps.chefmate.deeplink.DeepLinkCoordinator
import com.plusmobileapps.chefmate.deeplink.SchemeRegistrar
import com.plusmobileapps.chefmate.deeplink.SingleInstance
import com.plusmobileapps.chefmate.root.DeepLink
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.update.DesktopUpdater
import com.plusmobileapps.chefmate.update.UpdateBanner
import java.awt.Desktop
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce

private const val KEY_WINDOW_WIDTH = "window.width"
private const val KEY_WINDOW_HEIGHT = "window.height"
private const val KEY_WINDOW_X = "window.x"
private const val KEY_WINDOW_Y = "window.y"
private const val KEY_WINDOW_PLACEMENT = "window.placement"

@OptIn(ExperimentalTime::class, FlowPreview::class)
fun main(args: Array<String>) {
    // Windows/Linux spawn a fresh process for every `chefmate://…` open. If another instance is
    // already running, forward this launch's link to it and exit without opening a second window.
    if (!SingleInstance.acquireOrForward(args)) return

    // Make the OS route `chefmate://…` to this app. macOS uses the packaged Info.plist
    // (CFBundleURLTypes) + the Apple Event handler below; Windows/Linux self-register at runtime.
    SchemeRegistrar.registerIfPackaged()

    // macOS delivers URL opens as Apple Events, not command-line args, both at cold launch and
    // while
    // running. Route them through the coordinator so the warm-delivery collector navigates the app.
    // Throws on non-macOS / when unsupported, so it is guarded and swallowed.
    runCatching {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.APP_OPEN_URI)) {
            desktop.setOpenURIHandler { event -> DeepLinkCoordinator.submit(event.uri.toString()) }
        }
    }

    // Cold-start deep link from the launch arguments (Windows/Linux). On macOS argv is empty and
    // the
    // link instead arrives via the Apple Event handler above.
    val deepLink = DeepLink.parse(args.firstOrNull())
    // Initialize Bugsnag + Kermit logging for JVM
    BugsnagInitializer().initialize(BuildConfig.BUGSNAG_API_KEY)
    // Only initialize the lifecycle outside the application block
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()
    val appComponent = dev.zacsweers.metro.createGraph<JvmApplicationComponent>()
    val updater = DesktopUpdater()

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
                deepLink = deepLink,
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

        LaunchedEffect(Unit) { updater.checkForUpdates() }

        Window(
            onCloseRequest = {
                updater.close()
                exitApplication()
            },
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
            // Deep links that arrive while the app is running (macOS Apple Events, or links
            // forwarded
            // from a second launch on Windows/Linux) route into the live RootBloc and raise the
            // window. Cold-start links are already applied as the initial stack via [deepLink]
            // above.
            LaunchedEffect(rootBloc) {
                DeepLinkCoordinator.links.collect { url ->
                    rootBloc.handleDeepLink(url)
                    window.toFront()
                    window.requestFocus()
                }
            }
            Box(modifier = Modifier.fillMaxSize()) {
                App(rootBloc = rootBloc, toastService = appComponent.toastService)
                val updateState by updater.state.collectAsState()
                ChefMateTheme {
                    UpdateBanner(
                        state = updateState,
                        onDownload = updater::startDownload,
                        onInstall = updater::install,
                        onDismiss = updater::dismiss,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

private data class WindowSnapshot(
    val placement: WindowPlacement,
    val size: DpSize,
    val position: WindowPosition,
)
