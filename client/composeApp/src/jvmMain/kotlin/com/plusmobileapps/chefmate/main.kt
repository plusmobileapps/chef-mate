package com.plusmobileapps.chefmate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry


fun main() {
    val lifecycle = LifecycleRegistry()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Chef Mate",
        ) {
            App(
                DefaultBlocContext(
                    componentContext = DefaultComponentContext(
                        lifecycle = lifecycle
                    )
                )
            )
        }
    }
}