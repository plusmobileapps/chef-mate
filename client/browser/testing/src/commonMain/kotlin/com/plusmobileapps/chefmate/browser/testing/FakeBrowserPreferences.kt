package com.plusmobileapps.chefmate.browser.testing

import com.plusmobileapps.chefmate.browser.BrowserPreferences
import com.plusmobileapps.chefmate.browser.SearchEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBrowserPreferences : BrowserPreferences {
    private val _isHistoryEnabled = MutableStateFlow(true)
    override val isHistoryEnabled: StateFlow<Boolean> = _isHistoryEnabled.asStateFlow()

    private val _defaultSearchEngine = MutableStateFlow<SearchEngine?>(null)
    override val defaultSearchEngine: StateFlow<SearchEngine?> = _defaultSearchEngine.asStateFlow()

    override fun setHistoryEnabled(enabled: Boolean) {
        _isHistoryEnabled.value = enabled
    }

    override fun setDefaultSearchEngine(engine: SearchEngine) {
        _defaultSearchEngine.value = engine
    }
}
