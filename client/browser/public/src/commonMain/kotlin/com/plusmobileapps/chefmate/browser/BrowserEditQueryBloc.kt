package com.plusmobileapps.chefmate.browser

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface BrowserEditQueryBloc {
    val state: StateFlow<Model>

    fun onSearchTextChanged(text: String)

    fun onNavigate()

    fun onCancel()

    data class Model(val searchText: String = "")

    sealed class Output {
        data class Navigate(val url: String) : Output()

        data object Cancel : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
            initialText: String,
        ): BrowserEditQueryBloc
    }
}
