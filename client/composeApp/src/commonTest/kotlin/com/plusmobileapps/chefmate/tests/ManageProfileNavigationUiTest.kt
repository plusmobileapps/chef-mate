package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.profile.robots.manageProfile
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ManageProfileNavigationUiTest {

    @Test
    fun opening_manage_profile_from_more_tab_shows_the_screen() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickManageProfileRow()

        manageProfile().awaitDisplayed().assertDisplayed()
    }

    @Test
    fun saving_profile_returns_to_more_tab() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickManageProfileRow()

        manageProfile().awaitDisplayed().setDisplayName("Renamed Chef").tapSave()

        // Saving pops back to the More tab.
        more().awaitDisplayed()
    }
}
