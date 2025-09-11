package com.plusmobileapps.chefmate

import androidx.compose.ui.window.ComposeUIViewController
import com.plusmobileapps.chefmate.root.RootBloc

object MainViewController {
    fun create(rootBloc: RootBloc) =
        ComposeUIViewController {
            App(rootBloc)
        }
}
