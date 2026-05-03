package com.plusmobileapps.chefmate.browser.testing

import com.plusmobileapps.chefmate.browser.BrowserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBrowserPreferences : BrowserPreferences {
    private val _isHistoryEnabled = MutableStateFlow(true)
    override val isHistoryEnabled: StateFlow<Boolean> = _isHistoryEnabled.asStateFlow()

    override fun setHistoryEnabled(enabled: Boolean) {
        _isHistoryEnabled.value = enabled
    }
}
