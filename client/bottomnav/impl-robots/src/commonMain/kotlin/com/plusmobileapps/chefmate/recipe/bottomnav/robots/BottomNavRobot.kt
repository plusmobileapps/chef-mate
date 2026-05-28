@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.bottomnav.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavTestTags

/**
 * Robot for the bottom navigation bar. Each method matches the tab by its visible label — Material
 * 3's `NavigationBarItem` doesn't surface `Modifier.testTag` in the semantics tree, so the label is
 * the only reliable handle. Labels live in [BottomNavTestTags] so production strings.xml and these
 * robots stay in sync.
 */
class BottomNavRobot(private val test: ComposeUiTest) {

    fun clickRecipesTab(): BottomNavRobot = clickTab(BottomNavTestTags.RECIPES_TAB)

    fun clickGroceriesTab(): BottomNavRobot = clickTab(BottomNavTestTags.GROCERIES_TAB)

    fun clickMealsTab(): BottomNavRobot = clickTab(BottomNavTestTags.MEALS_TAB)

    fun clickBrowserTab(): BottomNavRobot = clickTab(BottomNavTestTags.BROWSER_TAB)

    fun clickMoreTab(): BottomNavRobot = clickTab(BottomNavTestTags.MORE_TAB)

    private fun clickTab(label: String): BottomNavRobot = apply {
        test.waitUntilExactlyOneExists(hasText(label))
        test.onNode(hasText(label)).performClick()
    }
}

fun ComposeUiTest.bottomNav(): BottomNavRobot = BottomNavRobot(this)
