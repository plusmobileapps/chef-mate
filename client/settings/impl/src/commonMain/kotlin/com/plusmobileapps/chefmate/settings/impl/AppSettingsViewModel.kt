package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.browser.BrowserHistoryRepository
import com.plusmobileapps.chefmate.browser.BrowserPreferences
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class AppSettingsViewModel(
    @Main mainContext: CoroutineContext,
    private val browserPreferences: BrowserPreferences,
    private val historyRepository: BrowserHistoryRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(AppSettingsBloc.Model())
    val state: StateFlow<AppSettingsBloc.Model> = _state.asStateFlow()

    init {
        browserPreferences.isHistoryEnabled
            .onEach { enabled -> _state.update { it.copy(isHistoryEnabled = enabled) } }
            .launchIn(scope)
    }

    fun setHistoryEnabled(enabled: Boolean) {
        browserPreferences.setHistoryEnabled(enabled)
    }

    fun showClearHistoryDialog() {
        _state.update { it.copy(showClearHistoryDialog = true) }
    }

    fun dismissClearHistoryDialog() {
        _state.update { it.copy(showClearHistoryDialog = false) }
    }

    fun clearHistory() {
        _state.update { it.copy(showClearHistoryDialog = false) }
        scope.launch { historyRepository.clearAll() }
    }
}
