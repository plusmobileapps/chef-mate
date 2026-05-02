package com.plusmobileapps.chefmate.browser

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface BrowserLandingBloc {
    val state: StateFlow<Model>

    fun onSearchTextChanged(text: String)

    fun onNavigate()

    fun onFocusChanged(isFocused: Boolean)

    fun onCancel()

    data class Model(val searchText: String = "", val isFocused: Boolean = false)

    sealed class Output {
        data class Navigate(val url: String) : Output()

        data object Cancel : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BrowserLandingBloc
    }
}
