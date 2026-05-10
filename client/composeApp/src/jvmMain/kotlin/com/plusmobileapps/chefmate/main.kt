@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.plusmobileapps.chefmate.buildconfig.BuildConfig
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun main() {
    // Initialize Bugsnag + Kermit logging for JVM
    BugsnagInitializer().initialize(BuildConfig.BUGSNAG_API_KEY)
    // Only initialize the lifecycle outside the application block
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()
    val appComponent = dev.zacsweers.metro.createGraph<JvmApplicationComponent>()

    application {
        // Initialize the DefaultComponentContext inside the application block
        // to ensure it runs on the main thread
        val rootBloc =
            buildRoot(
                componentContext =
                    DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher),
                applicationComponent = appComponent,
            )

        Window(
            onCloseRequest = ::exitApplication,
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
