@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.grocery.core.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailTestTags
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListTestTags

/**
 * Robot for interacting with and asserting on the grocery list screen, including the detail bottom
 * sheet that overlays it. Lives in `client/grocery/core/impl-robots` so cross-feature UI tests can
 * compose against domain-level vocabulary.
 *
 * Every list-screen lookup is scoped under [GroceryListTestTags.SCREEN]. Sheet content is matched
 * under [GroceryDetailTestTags.SHEET] because once the sheet is open it floats above the list and
 * lives in a separate semantic subtree.
 *
 * Construct via [groceryList] from inside a `runComposeUiTest { … }` block.
 */
class GroceryListRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(GroceryListTestTags.SCREEN))
    private val inDetailSheet = hasAnyAncestor(hasTestTag(GroceryDetailTestTags.SHEET))

    fun clickItem(displayName: String): GroceryListRobot = apply {
        test.onNode(hasText(displayName) and onScreen).assertIsDisplayed().performClick()
    }

    fun awaitDetailSheetDisplayed(): GroceryListRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(GroceryDetailTestTags.SHEET))
    }

    fun assertAisleSelected(label: String): GroceryListRobot = apply {
        test.onNode(hasText(label) and inDetailSheet).assertIsDisplayed()
    }
}

fun ComposeUiTest.groceryList(): GroceryListRobot = GroceryListRobot(this)
