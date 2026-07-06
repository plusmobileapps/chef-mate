@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.grocery.autocomplete.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.grocery.autocomplete.GroceryAutocompleteTestTags

/**
 * Robot for the Settings → Grocery autocomplete management screen. Every node lookup is scoped
 * under [GroceryAutocompleteTestTags.SCREEN] so a row label here never matches a like-named node on
 * another screen (e.g. the same ingredient name rendered in the grocery list).
 */
class GroceryAutocompleteRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(GroceryAutocompleteTestTags.SCREEN))

    fun awaitDisplayed(): GroceryAutocompleteRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(GroceryAutocompleteTestTags.SCREEN))
    }

    fun assertItemDisplayed(name: String): GroceryAutocompleteRobot = apply {
        test.onNode(hasText(name) and onScreen).assertIsDisplayed()
    }

    fun openAddField(): GroceryAutocompleteRobot = apply {
        test.onNode(hasTestTag(GroceryAutocompleteTestTags.ADD_BUTTON) and onScreen).performClick()
    }

    fun typeNewItem(name: String): GroceryAutocompleteRobot = apply {
        test
            .onNode(hasTestTag(GroceryAutocompleteTestTags.CREATE_FIELD) and onScreen)
            .performTextInput(name)
    }
}

fun ComposeUiTest.groceryAutocomplete(): GroceryAutocompleteRobot = GroceryAutocompleteRobot(this)
