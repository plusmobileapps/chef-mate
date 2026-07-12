@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserSelectEngineBloc
import com.plusmobileapps.chefmate.browser.SearchEngine
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BrowserSelectEngineBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<BrowserSelectEngineBloc.Output>()
    private val browserPreferences = BrowserPreferencesImpl(MapSettings())
    private val bloc =
        BrowserSelectEngineBlocImpl(
            context = context,
            output = output,
            browserPreferences = browserPreferences,
        )

    @Test
    fun When_engine_selected_Then_persisted_to_preferences() {
        bloc.onEngineSelected(SearchEngine.BRAVE)
        browserPreferences.defaultSearchEngine.value shouldBe SearchEngine.BRAVE
    }

    @Test
    fun When_engine_selected_Then_engine_selected_output_emitted() {
        bloc.onEngineSelected(SearchEngine.DUCK_DUCK_GO)
        output.lastValue shouldBe BrowserSelectEngineBloc.Output.EngineSelected
    }
}
