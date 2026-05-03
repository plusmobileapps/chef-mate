package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BROWSER_HISTORY_ENABLED_KEY
import com.plusmobileapps.chefmate.di.AppScope
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
class BrowserPreferences(private val settings: Settings) {

    private val _isHistoryEnabled =
        MutableStateFlow(settings.getBoolean(BROWSER_HISTORY_ENABLED_KEY, defaultValue = true))

    val isHistoryEnabled: StateFlow<Boolean> = _isHistoryEnabled.asStateFlow()

    fun setHistoryEnabled(enabled: Boolean) {
        settings.putBoolean(BROWSER_HISTORY_ENABLED_KEY, enabled)
        _isHistoryEnabled.value = enabled
    }
}
