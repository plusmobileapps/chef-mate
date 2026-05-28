package com.plusmobileapps.chefmate.recipe.bottomnav

/**
 * Stable tab labels for use by UI tests. Matches the visible Compose label so the bottom-nav robot
 * can find each tab without a test tag — Material 3's `NavigationBarItem` doesn't surface
 * `Modifier.testTag` in the semantics tree, so labels are the most reliable handle.
 */
object BottomNavTestTags {
    const val RECIPES_TAB = "Recipes"
    const val GROCERIES_TAB = "Grocery"
    const val MEALS_TAB = "Meals"
    const val BROWSER_TAB = "Browser"
    const val MORE_TAB = "More"
}
