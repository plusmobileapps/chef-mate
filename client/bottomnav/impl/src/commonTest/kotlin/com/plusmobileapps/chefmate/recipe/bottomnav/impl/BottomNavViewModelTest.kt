@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.DEFAULT_TAB_ORDER
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class BottomNavViewModelTest {

    private val mainContext = UnconfinedTestDispatcher()

    @Test
    fun initial_tabs_are_seeded_from_preferences() {
        val customOrder =
            listOf(
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.RECIPES,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.BROWSER,
            )
        val prefs = FakeTabOrderPreferences(initial = customOrder)
        val vm = BottomNavViewModel(mainContext = mainContext, tabOrderPreferences = prefs)
        vm.state.value.tabs shouldBe customOrder
    }

    @Test
    fun preferences_updates_propagate_to_state() {
        val prefs = FakeTabOrderPreferences()
        val vm = BottomNavViewModel(mainContext = mainContext, tabOrderPreferences = prefs)
        vm.state.value.tabs shouldBe DEFAULT_TAB_ORDER

        val newOrder =
            listOf(
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.RECIPES,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.BROWSER,
                BottomNavBloc.Tab.SETTINGS,
            )
        prefs.setTabOrder(newOrder)
        vm.state.value.tabs shouldBe newOrder
    }
}
