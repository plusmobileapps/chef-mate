package com.plusmobileapps.chefmate.admin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "ChefMate Admin") { AdminApp() }
}
