@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.grocery.core.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.grocery.core.edit.EditGroceryListTestTags

/**
 * Robot for the Edit Grocery List screen. Scoped under [EditGroceryListTestTags.SCREEN]. Construct
 * via [editGroceryList] from inside a `runComposeUiTest { … }` block.
 */
class EditGroceryListRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(EditGroceryListTestTags.SCREEN))

    fun awaitDisplayed(): EditGroceryListRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(EditGroceryListTestTags.SCREEN))
    }

    /** Signed-out collaboration state shows the Sign in CTA. */
    fun assertSignInShown(): EditGroceryListRobot = apply {
        test.waitUntilExactlyOneExists(
            hasTestTag(EditGroceryListTestTags.SIGN_IN_BUTTON) and onScreen
        )
    }

    /** Signed-in collaboration state shows the invite-by-email field. */
    fun assertInviteFieldShown(): EditGroceryListRobot = apply {
        test.waitUntilExactlyOneExists(
            hasTestTag(EditGroceryListTestTags.INVITE_FIELD) and onScreen
        )
    }

    fun clickDelete(): EditGroceryListRobot = apply {
        test
            .onNode(hasTestTag(EditGroceryListTestTags.DELETE_BUTTON) and onScreen)
            .assertIsDisplayed()
            .performClick()
    }
}

fun ComposeUiTest.editGroceryList(): EditGroceryListRobot = EditGroceryListRobot(this)
