package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.family.robots.family
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FamilyNavigationUiTest {

    @Test
    fun opening_family_from_more_tab_shows_the_screen() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickFamilyRow()

        // A fresh test user isn't in a family, so the screen opens on the create form.
        family().awaitDisplayed().assertDisplayed().assertCreateFormShown()
    }
}
