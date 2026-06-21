@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.BrowserLandingBloc
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BrowserLandingBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<BrowserLandingBloc.Output>()
    private val coachMarkController = CoachMarkController(MapSettings())
    private val bloc =
        BrowserLandingBlocImpl(
            context = context,
            output = output,
            coachMarkController = coachMarkController,
        )

    @Test
    fun When_search_field_focused_Then_open_edit_query_emitted() {
        bloc.onSearchFieldFocused()
        output.lastValue shouldBe BrowserLandingBloc.Output.OpenEditQuery
    }

    @Test
    fun When_screen_opens_Then_search_coach_mark_active() {
        bloc.state.value.activeCoachMark shouldBe CoachMarkId.BROWSER_SEARCH
    }

    @Test
    fun When_search_field_focused_Then_coach_mark_dismissed_and_persisted() {
        bloc.onSearchFieldFocused()

        coachMarkController.hasSeen(CoachMarkId.BROWSER_SEARCH) shouldBe true
        bloc.state.value.activeCoachMark shouldBe null
    }
}
