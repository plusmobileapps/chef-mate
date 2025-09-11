package com.plusmobileapps.chefmate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry


fun main() {
    // Only initialize the lifecycle outside the application block
    val lifecycle = LifecycleRegistry()
    val appComponent = JvmApplicationComponent::class.create()

    application {
        // Initialize the DefaultComponentContext inside the application block
        // to ensure it runs on the main thread
        val rootBloc = buildRoot(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            applicationComponent = appComponent,
        )

        Window(
            onCloseRequest = ::exitApplication,
            title = "Chef Mate",
        ) {
            App(rootBloc = rootBloc)
        }
    }
}