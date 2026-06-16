@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.backhandler.BackDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lifecycle = LifecycleRegistry()
    val backDispatcher = BackDispatcher()
    val appComponent = dev.zacsweers.metro.createGraph<WasmApplicationComponent>()

    val rootBloc =
        buildRoot(
            componentContext =
                DefaultComponentContext(lifecycle = lifecycle, backHandler = backDispatcher),
            applicationComponent = appComponent,
        )

    lifecycle.resume()

    ComposeViewport(document.body!!) { App(rootBloc = rootBloc) }
}
