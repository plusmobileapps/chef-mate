@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.browser.BrowserHistoryEntry
import com.plusmobileapps.chefmate.browser.BrowserHistoryRepository
import com.plusmobileapps.chefmate.browser.testing.FakeBrowserPreferences
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class AppSettingsBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<AppSettingsBloc.Output>()
    private val browserPreferences = FakeBrowserPreferences()
    private val historyRepository: BrowserHistoryRepository =
        mock(
            block = {
                everySuspend { observeRecent(any()) } returns
                    MutableStateFlow<List<BrowserHistoryEntry>>(emptyList())
                everySuspend { clearAll() } returns Unit
            }
        )

    private fun createBloc() =
        AppSettingsBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                AppSettingsViewModel(
                    mainContext = context.mainContext,
                    browserPreferences = browserPreferences,
                    historyRepository = historyRepository,
                )
            },
        )

    @Test
    fun initial_state_reflects_history_enabled_preference() {
        browserPreferences.setHistoryEnabled(false)
        val bloc = createBloc()
        bloc.state.value.isHistoryEnabled shouldBe false
    }

    @Test
    fun When_history_toggled_off_Then_preferences_updated() {
        val bloc = createBloc()
        bloc.onHistoryEnabledChanged(false)
        browserPreferences.isHistoryEnabled.value shouldBe false
        bloc.state.value.isHistoryEnabled shouldBe false
    }

    @Test
    fun When_clear_history_clicked_Then_dialog_shown() {
        val bloc = createBloc()
        bloc.onClearHistoryClicked()
        bloc.state.value.showClearHistoryDialog shouldBe true
    }

    @Test
    fun When_clear_history_dismissed_Then_dialog_hidden() {
        val bloc = createBloc()
        bloc.onClearHistoryClicked()
        bloc.onClearHistoryDismissed()
        bloc.state.value.showClearHistoryDialog shouldBe false
    }

    @Test
    fun When_clear_history_confirmed_Then_repository_clearAll_called() = runTest {
        val bloc = createBloc()
        bloc.onClearHistoryClicked()
        bloc.onClearHistoryConfirmed()
        bloc.state.value.showClearHistoryDialog shouldBe false
        verifySuspend { historyRepository.clearAll() }
    }

    @Test
    fun When_back_clicked_Then_back_output_emitted() {
        val bloc = createBloc()
        bloc.onBack()
        output.lastValue shouldBe AppSettingsBloc.Output.Back
    }
}
