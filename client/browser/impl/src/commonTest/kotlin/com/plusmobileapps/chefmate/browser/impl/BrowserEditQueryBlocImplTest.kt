@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserEditQueryBloc
import com.plusmobileapps.chefmate.browser.BrowserHistoryEntry
import com.plusmobileapps.chefmate.browser.BrowserHistoryRepository
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.russhwolf.settings.MapSettings
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

class BrowserEditQueryBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<BrowserEditQueryBloc.Output>()
    private val historyFlow = MutableStateFlow<List<BrowserHistoryEntry>>(emptyList())
    private val historyRepository: BrowserHistoryRepository =
        mock(
            block = {
                everySuspend { observeRecent(any()) } returns historyFlow
                everySuspend { deleteById(any()) } returns Unit
            }
        )
    private val browserPreferences = BrowserPreferences(MapSettings())

    private fun createBloc(initialText: String = "") =
        BrowserEditQueryBlocImpl(
            context = context,
            output = output,
            initialText = initialText,
            viewModelFactory = {
                BrowserEditQueryViewModel(
                    mainContext = context.mainContext,
                    historyRepository = historyRepository,
                    browserPreferences = browserPreferences,
                )
            },
        )

    @Test
    fun When_initialized_with_initial_text_Then_state_is_seeded() {
        val bloc = createBloc(initialText = "https://example.com")
        bloc.state.value.searchText shouldBe "https://example.com"
    }

    @Test
    fun When_initialized_with_empty_text_Then_state_is_empty() {
        val bloc = createBloc(initialText = "")
        bloc.state.value.searchText shouldBe ""
    }

    @Test
    fun When_search_text_changed_Then_state_reflects_text() {
        val bloc = createBloc()
        bloc.onSearchTextChanged("typed")
        bloc.state.value.searchText shouldBe "typed"
    }

    @Test
    fun When_navigate_with_full_url_Then_navigate_output_emitted() {
        val bloc = createBloc()
        bloc.onSearchTextChanged("https://example.com")
        bloc.onNavigate()
        output.lastValue shouldBe BrowserEditQueryBloc.Output.Navigate("https://example.com")
    }

    @Test
    fun When_navigate_with_bare_domain_Then_https_prepended() {
        val bloc = createBloc()
        bloc.onSearchTextChanged("example.com")
        bloc.onNavigate()
        output.lastValue shouldBe BrowserEditQueryBloc.Output.Navigate("https://example.com")
    }

    @Test
    fun When_navigate_with_search_query_Then_routed_to_google_search() {
        val bloc = createBloc()
        bloc.onSearchTextChanged("serious eats soup")
        bloc.onNavigate()
        output.lastValue shouldBe
            BrowserEditQueryBloc.Output.Navigate(
                "https://www.google.com/search?q=serious+eats+soup"
            )
    }

    @Test
    fun When_navigate_with_blank_text_Then_no_output_emitted() {
        val bloc = createBloc()
        bloc.onSearchTextChanged("")
        bloc.onNavigate()
        output.values shouldBe emptyList()
    }

    @Test
    fun When_cancel_Then_cancel_output_emitted() {
        val bloc = createBloc(initialText = "anything")
        bloc.onCancel()
        output.lastValue shouldBe BrowserEditQueryBloc.Output.Cancel
    }

    @Test
    fun When_history_flow_emits_Then_state_reflects_history() {
        val bloc = createBloc()
        val entry = BrowserHistoryEntry(id = 1, url = "https://example.com", visitedAt = "now")
        historyFlow.value = listOf(entry)
        bloc.state.value.history shouldBe listOf(entry)
    }

    @Test
    fun When_history_disabled_Then_state_history_is_empty() {
        val entry = BrowserHistoryEntry(id = 1, url = "https://example.com", visitedAt = "now")
        historyFlow.value = listOf(entry)
        browserPreferences.setHistoryEnabled(false)
        val bloc = createBloc()
        bloc.state.value.history shouldBe emptyList()
    }

    @Test
    fun When_history_item_clicked_Then_navigate_emitted_with_stored_url() {
        val bloc = createBloc()
        val entry =
            BrowserHistoryEntry(id = 1, url = "https://example.com/recipe", visitedAt = "now")
        bloc.onHistoryItemClicked(entry)
        output.lastValue shouldBe BrowserEditQueryBloc.Output.Navigate("https://example.com/recipe")
    }

    @Test
    fun When_history_item_delete_clicked_Then_repository_delete_called() = runTest {
        val bloc = createBloc()
        val entry = BrowserHistoryEntry(id = 42, url = "https://example.com", visitedAt = "now")
        bloc.onHistoryItemDeleteClicked(entry)
        verifySuspend { historyRepository.deleteById(42) }
    }
}
