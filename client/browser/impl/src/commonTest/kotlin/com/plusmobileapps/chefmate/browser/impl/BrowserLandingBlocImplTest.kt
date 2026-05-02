@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BrowserLandingBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<BrowserLandingBloc.Output>()

    private val bloc =
        BrowserLandingBlocImpl(
            context = context,
            output = output,
            viewModelFactory = { BrowserLandingViewModel(mainContext = context.mainContext) },
        )

    @Test
    fun When_search_text_changed_Then_state_reflects_text() {
        bloc.onSearchTextChanged("hello")
        bloc.state.value.searchText shouldBe "hello"
    }

    @Test
    fun When_navigate_with_full_url_Then_navigate_output_emitted_with_url() {
        bloc.onSearchTextChanged("https://example.com")
        bloc.onNavigate()
        output.lastValue shouldBe BrowserLandingBloc.Output.Navigate("https://example.com")
    }

    @Test
    fun When_navigate_with_bare_domain_Then_https_prepended() {
        bloc.onSearchTextChanged("example.com")
        bloc.onNavigate()
        output.lastValue shouldBe BrowserLandingBloc.Output.Navigate("https://example.com")
    }

    @Test
    fun When_navigate_with_search_query_Then_routed_to_google_search() {
        bloc.onSearchTextChanged("chicken pasta")
        bloc.onNavigate()
        output.lastValue shouldBe
            BrowserLandingBloc.Output.Navigate("https://www.google.com/search?q=chicken+pasta")
    }

    @Test
    fun When_navigate_with_blank_text_Then_no_output_emitted() {
        bloc.onSearchTextChanged("   ")
        bloc.onNavigate()
        output.values shouldBe emptyList()
    }

    @Test
    fun When_search_field_focused_Then_open_edit_query_emitted_with_current_text() {
        bloc.onSearchTextChanged("recipes")
        bloc.onSearchFieldFocused()
        output.lastValue shouldBe BrowserLandingBloc.Output.OpenEditQuery("recipes")
    }

    @Test
    fun When_search_field_focused_with_empty_text_Then_open_edit_query_emitted_with_empty_text() {
        bloc.onSearchFieldFocused()
        output.lastValue shouldBe BrowserLandingBloc.Output.OpenEditQuery("")
    }
}
