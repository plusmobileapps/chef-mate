@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.browser.BrowserEditQueryBloc
import com.plusmobileapps.chefmate.browser.BrowserHistoryEntry
import com.plusmobileapps.chefmate.browser.BrowserHistoryRepository
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class BrowserEditQueryViewModel(
    @Main mainContext: CoroutineContext,
    private val historyRepository: BrowserHistoryRepository,
    private val browserPreferences: BrowserPreferences,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(BrowserEditQueryBloc.Model())
    val state: StateFlow<BrowserEditQueryBloc.Model> = _state.asStateFlow()

    init {
        browserPreferences.isHistoryEnabled
            .flatMapLatest { enabled ->
                if (enabled) historyRepository.observeRecent() else flowOf(emptyList())
            }
            .onEach { history -> _state.update { it.copy(history = history) } }
            .launchIn(scope)
    }

    fun onSearchTextChanged(text: String) {
        _state.update { it.copy(searchText = text) }
    }

    fun deleteHistoryEntry(entry: BrowserHistoryEntry) {
        scope.launch { historyRepository.deleteById(entry.id) }
    }
}
