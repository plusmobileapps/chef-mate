package com.plusmobileapps.chefmate.recipe.bottomnav

import kotlinx.coroutines.flow.StateFlow

const val BOTTOM_NAV_TAB_ORDER_KEY = "bottomnav.tab.order"

val DEFAULT_TAB_ORDER: List<BottomNavBloc.Tab> =
    listOf(
        BottomNavBloc.Tab.RECIPES,
        BottomNavBloc.Tab.GROCERIES,
        BottomNavBloc.Tab.MEALS,
        BottomNavBloc.Tab.BROWSER,
        BottomNavBloc.Tab.SETTINGS,
    )

/**
 * Stable identifier for the tab, decoupled from enum ordinal so persistence survives reordering.
 */
val BottomNavBloc.Tab.stableId: String
    get() =
        when (this) {
            BottomNavBloc.Tab.RECIPES -> "recipes"
            BottomNavBloc.Tab.GROCERIES -> "groceries"
            BottomNavBloc.Tab.MEALS -> "meal_plan"
            BottomNavBloc.Tab.BROWSER -> "browser"
            BottomNavBloc.Tab.SETTINGS -> "more"
        }

fun tabFromStableId(id: String): BottomNavBloc.Tab? =
    BottomNavBloc.Tab.entries.firstOrNull { it.stableId == id }

interface TabOrderPreferences {
    val tabOrder: StateFlow<List<BottomNavBloc.Tab>>

    fun setTabOrder(tabs: List<BottomNavBloc.Tab>)
}
