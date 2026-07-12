package com.plusmobileapps.chefmate.browser

import kotlinx.coroutines.flow.StateFlow

interface BrowserPreferences {
    val isHistoryEnabled: StateFlow<Boolean>

    /** The user's chosen default search engine, or null until they pick one on first run. */
    val defaultSearchEngine: StateFlow<SearchEngine?>

    fun setHistoryEnabled(enabled: Boolean)

    fun setDefaultSearchEngine(engine: SearchEngine)
}
