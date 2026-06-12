@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.grocery.core.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilAtLeastOneExists
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

    /** Opens the list selector (bottom sheet on phones, dropdown on tablets). */
    fun openListSelector(): GroceryListRobot = apply {
        test
            .onNode(hasTestTag(GroceryListTestTags.LIST_SELECTOR) and onScreen)
            .assertIsDisplayed()
            .performClick()
    }

    /**
     * Clicks the edit (pencil) icon on the first list row in the open selector. The selector floats
     * in its own subtree (sheet or dropdown popup), so this is matched by content description
     * rather than scoped to the list screen.
     */
    fun clickEditFirstList(editContentDescription: String): GroceryListRobot = apply {
        test.waitUntilAtLeastOneExists(hasContentDescription(editContentDescription))
        test.onAllNodes(hasContentDescription(editContentDescription)).onFirst().performClick()
    }
}

fun ComposeUiTest.groceryList(): GroceryListRobot = GroceryListRobot(this)
