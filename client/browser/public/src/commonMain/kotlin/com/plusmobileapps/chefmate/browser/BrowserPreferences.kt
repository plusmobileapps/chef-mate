package com.plusmobileapps.chefmate.browser

import kotlinx.coroutines.flow.StateFlow

interface BrowserPreferences {
    val isHistoryEnabled: StateFlow<Boolean>

    fun setHistoryEnabled(enabled: Boolean)
}
