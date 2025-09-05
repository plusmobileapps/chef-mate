package com.plusmobileapps.chefmate

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
<<<<<<< HEAD
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry


fun main() {
    val lifecycle = LifecycleRegistry()
    val rootBloc = buildRoot(DefaultComponentContext(lifecycle))
    application {
=======
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import java.io.File

private const val SAVED_STATE_FILE_NAME = "saved_state.dat"

fun main() {
    val lifecycle = LifecycleRegistry()
    val stateKeeper =
        StateKeeperDispatcher(File(SAVED_STATE_FILE_NAME).readSerializableContainer())

    application {


>>>>>>> cd10bf7 (add persistence with sqldelight and integrate into android client)
        Window(
            onCloseRequest = ::exitApplication,
            title = "Chef Mate",
        ) {
<<<<<<< HEAD
            App(rootBloc = rootBloc)
=======
            App(DefaultBlocContext())
>>>>>>> cd10bf7 (add persistence with sqldelight and integrate into android client)
        }
    }
}