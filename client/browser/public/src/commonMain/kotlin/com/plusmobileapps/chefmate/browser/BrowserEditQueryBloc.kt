package com.plusmobileapps.chefmate.browser

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface BrowserEditQueryBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        BrowserEditQueryScreen(bloc = this, modifier = modifier)
    }

    fun onSearchTextChanged(text: String)

    fun onNavigate()

    fun onCancel()

    fun onHistoryItemClicked(entry: BrowserHistoryEntry)

    fun onHistoryItemDeleteClicked(entry: BrowserHistoryEntry)

    data class Model(
        val searchText: String = "",
        val history: ImmutableList<BrowserHistoryEntry> = persistentListOf(),
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
