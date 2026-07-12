package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.browser.SearchEngine
import com.plusmobileapps.chefmate.browser.robots.browser
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BrowserSearchEngineNavigationUiTest {

    @Test
    fun first_run_requires_choosing_a_search_engine_before_landing() = runRootBlocTest {
        // Settings are cleared by the harness, so no default engine is set yet.
        bottomNav().clickBrowserTab()

        browser()
            .awaitSelectEngineDisplayed()
            .selectEngine(SearchEngine.DUCK_DUCK_GO)
            .awaitLandingDisplayed()
            .assertLandingEngine(SearchEngine.DUCK_DUCK_GO)
    }
}
