@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BROWSER_HISTORY_ENABLED_KEY
import com.plusmobileapps.chefmate.browser.BROWSER_SEARCH_ENGINE_KEY
import com.plusmobileapps.chefmate.browser.SearchEngine
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BrowserPreferencesTest {

    @Test
    fun isHistoryEnabled_defaults_to_true() {
        val preferences = BrowserPreferencesImpl(MapSettings())
        preferences.isHistoryEnabled.value shouldBe true
    }

    @Test
    fun isHistoryEnabled_reads_existing_value_from_settings() {
        val settings = MapSettings()
        settings.putBoolean(BROWSER_HISTORY_ENABLED_KEY, false)
        val preferences = BrowserPreferencesImpl(settings)
        preferences.isHistoryEnabled.value shouldBe false
    }

    @Test
    fun setHistoryEnabled_updates_state_flow_and_settings() {
        val settings = MapSettings()
        val preferences = BrowserPreferencesImpl(settings)

        preferences.setHistoryEnabled(false)

        preferences.isHistoryEnabled.value shouldBe false
        settings.getBoolean(BROWSER_HISTORY_ENABLED_KEY, defaultValue = true) shouldBe false
    }

    @Test
    fun defaultSearchEngine_defaults_to_null() {
        val preferences = BrowserPreferencesImpl(MapSettings())
        preferences.defaultSearchEngine.value shouldBe null
    }

    @Test
    fun defaultSearchEngine_reads_existing_value_from_settings() {
        val settings = MapSettings()
        settings.putString(BROWSER_SEARCH_ENGINE_KEY, SearchEngine.DUCK_DUCK_GO.id)
        val preferences = BrowserPreferencesImpl(settings)
        preferences.defaultSearchEngine.value shouldBe SearchEngine.DUCK_DUCK_GO
    }

    @Test
    fun setDefaultSearchEngine_updates_state_flow_and_settings() {
        val settings = MapSettings()
        val preferences = BrowserPreferencesImpl(settings)

        preferences.setDefaultSearchEngine(SearchEngine.BING)

        preferences.defaultSearchEngine.value shouldBe SearchEngine.BING
        settings.getStringOrNull(BROWSER_SEARCH_ENGINE_KEY) shouldBe SearchEngine.BING.id
    }
}
