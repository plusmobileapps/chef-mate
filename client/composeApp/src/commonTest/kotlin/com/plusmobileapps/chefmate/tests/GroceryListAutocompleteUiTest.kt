@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.grocery.core.robots.groceryList
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.bottomnav.robots.bottomNav
import kotlin.test.Test

class GroceryListAutocompleteUiTest {

    @Test
    fun selecting_autocomplete_suggestion_fills_item_input() = runRootBlocTest {
        bottomNav().clickGroceriesTab()

        groceryList()
            .enterItemNamePrefix("stra")
            .clickItemSuggestion("Strawberries")
            .assertItemInputText("Strawberries")
    }
}
