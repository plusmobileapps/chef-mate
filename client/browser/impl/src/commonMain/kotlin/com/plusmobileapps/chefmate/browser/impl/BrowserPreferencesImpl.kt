package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BROWSER_HISTORY_ENABLED_KEY
import com.plusmobileapps.chefmate.browser.BROWSER_SEARCH_ENGINE_KEY
import com.plusmobileapps.chefmate.browser.BrowserPreferences
import com.plusmobileapps.chefmate.browser.SearchEngine
import com.plusmobileapps.chefmate.di.AppScope
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class BrowserPreferencesImpl(private val settings: Settings) : BrowserPreferences {

    private val _isHistoryEnabled =
        MutableStateFlow(settings.getBoolean(BROWSER_HISTORY_ENABLED_KEY, defaultValue = true))

    override val isHistoryEnabled: StateFlow<Boolean> = _isHistoryEnabled.asStateFlow()

    private val _defaultSearchEngine =
        MutableStateFlow(SearchEngine.fromId(settings.getStringOrNull(BROWSER_SEARCH_ENGINE_KEY)))

    override val defaultSearchEngine: StateFlow<SearchEngine?> = _defaultSearchEngine.asStateFlow()

    override fun setHistoryEnabled(enabled: Boolean) {
        settings.putBoolean(BROWSER_HISTORY_ENABLED_KEY, enabled)
        _isHistoryEnabled.value = enabled
    }

    override fun setDefaultSearchEngine(engine: SearchEngine) {
        settings.putString(BROWSER_SEARCH_ENGINE_KEY, engine.id)
        _defaultSearchEngine.value = engine
    }
}
