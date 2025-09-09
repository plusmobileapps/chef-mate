package com.plusmobileapps.chefmate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry


fun main() {
    val lifecycle = LifecycleRegistry()
    val appComponent = JvmApplicationComponent::class.create()
    val rootBloc = buildRoot(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        applicationComponent = appComponent,
    )
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Chef Mate",
        ) {
            App(rootBloc = rootBloc)
        }
    }
}