package com.plusmobileapps.chefmate.browser

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer

interface BrowserLandingBloc {
    fun onSearchFieldFocused()

    sealed class Output {
        data object OpenEditQuery : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BrowserLandingBloc
    }
}
