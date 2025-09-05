package com.plusmobileapps.chefmate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import java.io.File

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

fun main() {
    val lifecycle = LifecycleRegistry()
    val stateKeeper =
        StateKeeperDispatcher(File(SAVED_STATE_FILE_NAME).readSerializableContainer())

    application {


        Window(
            onCloseRequest = ::exitApplication,
            title = "Chef Mate",
        ) {
            App(DefaultBlocContext())
        }
    }
}