@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.onboarding.demo

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry

fun main() {
    // Initialize the lifecycle outside the application block, as the main composeApp does.
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()

    application {
        // Build the ComponentContext inside the application block so it runs on the main thread.
        val bloc =
            buildOnboardingDemoBloc(
                componentContext =
                    DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher)
            )

        Window(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(size = DpSize(420.dp, 800.dp)),
            title = "Onboarding Demo",
        ) {
            OnboardingDemoApp(bloc)
        }
    }
}
