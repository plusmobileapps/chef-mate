package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import com.plusmobileapps.chefmate.settings.root.robots.settingsRoot
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettingsRootNavigationUiTest {

    @Test
    fun opening_settings_then_bottom_nav_order_navigates_within_settings_root() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickAppSettingsRow()

        settingsRoot().awaitDisplayed().clickRow("Bottom navigation order")

        // The BottomNavOrder screen lists every tab; "Recipes" is unique to it and never appears on
        // the AppSettings list, so it confirms we navigated into the detail screen.
        settingsRoot().assertRowDisplayed("Recipes")
    }
}
