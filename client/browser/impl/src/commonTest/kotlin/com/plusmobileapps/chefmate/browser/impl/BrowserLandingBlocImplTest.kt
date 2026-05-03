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
    private val bloc = BrowserLandingBlocImpl(context = context, output = output)

    @Test
    fun When_search_field_focused_Then_open_edit_query_emitted() {
        bloc.onSearchFieldFocused()
        output.lastValue shouldBe BrowserLandingBloc.Output.OpenEditQuery
    }
}
