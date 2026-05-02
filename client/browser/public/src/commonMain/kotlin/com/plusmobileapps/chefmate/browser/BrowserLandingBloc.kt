package com.plusmobileapps.chefmate.browser

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface BrowserLandingBloc {
    val state: StateFlow<Model>

    fun onSearchTextChanged(text: String)

    fun onNavigate()

    fun onSearchFieldFocused()

    data class Model(val searchText: String = "")

    sealed class Output {
        data class Navigate(val url: String) : Output()

        data class OpenEditQuery(val initialText: String) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BrowserLandingBloc
    }
}
