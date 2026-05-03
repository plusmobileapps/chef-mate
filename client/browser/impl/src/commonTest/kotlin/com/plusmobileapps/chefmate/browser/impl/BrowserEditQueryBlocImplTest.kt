@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserEditQueryBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BrowserEditQueryBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<BrowserEditQueryBloc.Output>()

    private fun createBloc(initialText: String = "") =
        BrowserEditQueryBlocImpl(
            context = context,
            output = output,
            initialText = initialText,
            viewModelFactory = { BrowserEditQueryViewModel(mainContext = context.mainContext) },
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
}
