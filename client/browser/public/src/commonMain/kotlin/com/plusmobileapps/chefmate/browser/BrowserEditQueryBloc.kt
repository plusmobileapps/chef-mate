package com.plusmobileapps.chefmate.browser

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface BrowserEditQueryBloc : BlocScreen {
    val state: StateFlow<Model>

    fun onSearchTextChanged(text: String)

    fun onNavigate()

    fun onCancel()

    fun onHistoryItemClicked(entry: BrowserHistoryEntry)

    fun onHistoryItemDeleteClicked(entry: BrowserHistoryEntry)

    data class Model(
        val searchText: String = "",
        val history: List<BrowserHistoryEntry> = emptyList(),
    )

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
