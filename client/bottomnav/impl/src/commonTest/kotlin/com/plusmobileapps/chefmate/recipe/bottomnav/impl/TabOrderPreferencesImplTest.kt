@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.recipe.bottomnav.BOTTOM_NAV_TAB_ORDER_KEY
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.DEFAULT_TAB_ORDER
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class TabOrderPreferencesImplTest {

    @Test
    fun defaults_to_canonical_order_when_no_persisted_value() {
        val prefs = TabOrderPreferencesImpl(MapSettings())
        prefs.tabOrder.value shouldBe DEFAULT_TAB_ORDER
    }

    @Test
    fun reads_persisted_order_from_settings() {
        val settings = MapSettings()
        settings.putString(BOTTOM_NAV_TAB_ORDER_KEY, "more,recipes,groceries,meal_plan,browser")
        val prefs = TabOrderPreferencesImpl(settings)
        prefs.tabOrder.value shouldBe
            listOf(
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.RECIPES,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.BROWSER,
            )
    }

    @Test
    fun unknown_ids_are_dropped_and_missing_tabs_appended() {
        val settings = MapSettings()
        // Only two known ids, plus an unknown — the rest should append in canonical order.
        settings.putString(BOTTOM_NAV_TAB_ORDER_KEY, "more,unknown,recipes")
        val prefs = TabOrderPreferencesImpl(settings)
        prefs.tabOrder.value shouldBe
            listOf(
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.RECIPES,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.BROWSER,
            )
    }

    @Test
    fun setTabOrder_persists_stable_ids_and_updates_state_flow() {
        val settings = MapSettings()
        val prefs = TabOrderPreferencesImpl(settings)

        prefs.setTabOrder(
            listOf(
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.BROWSER,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.RECIPES,
            )
        )

        settings.getString(BOTTOM_NAV_TAB_ORDER_KEY, "") shouldBe
            "more,browser,meal_plan,groceries,recipes"
        prefs.tabOrder.value shouldBe
            listOf(
                BottomNavBloc.Tab.SETTINGS,
                BottomNavBloc.Tab.BROWSER,
                BottomNavBloc.Tab.MEALS,
                BottomNavBloc.Tab.GROCERIES,
                BottomNavBloc.Tab.RECIPES,
            )
    }
}
