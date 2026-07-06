package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.grocery.autocomplete.robots.groceryAutocomplete
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import com.plusmobileapps.chefmate.settings.robots.more
import com.plusmobileapps.chefmate.settings.root.robots.settingsRoot
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class GroceryAutocompleteNavigationUiTest {

    @Test
    fun opening_settings_then_autocomplete_lands_on_the_management_screen() = runRootBlocTest {
        bottomNav().clickMoreTab()
        more().awaitDisplayed().clickAppSettingsRow()

        settingsRoot().awaitDisplayed().clickRow("Autocomplete items")

        // The "Defaults" section header always renders near the top of the list, so it's a stable
        // signal the management screen loaded ("Apple" and friends sit too far down the lazy list
        // to be composed in the test viewport).
        groceryAutocomplete().awaitDisplayed().assertItemDisplayed("Defaults")
    }
}
