package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

interface BrowserLandingBloc : ComposeScreen {

    @Composable
    override fun Content(modifier: Modifier) {
        BrowserLandingScreen(bloc = this, modifier = modifier)
    }

    fun onSearchFieldFocused()

    sealed class Output {
        data object OpenEditQuery : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BrowserLandingBloc
    }
}
