package com.plusmobileapps.chefmate

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.root.RootBloc

fun MainViewController(rootBloc: RootBloc) = ComposeUIViewController {
    App(rootBloc)
}