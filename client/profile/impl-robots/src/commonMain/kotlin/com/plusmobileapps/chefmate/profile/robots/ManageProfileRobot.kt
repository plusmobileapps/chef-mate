@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.profile.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.profile.ManageProfileTestTags

/**
 * Robot for the Manage Profile screen. Every lookup is scoped to a descendant of
 * [ManageProfileTestTags.SCREEN] so titles rendered on other screens don't satisfy matchers.
 *
 * Construct via [manageProfile] from inside a `runComposeUiTest { … }` block.
 */
class ManageProfileRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(ManageProfileTestTags.SCREEN))

    fun awaitDisplayed(): ManageProfileRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(ManageProfileTestTags.SCREEN))
    }

    fun setDisplayName(name: String): ManageProfileRobot = apply {
        // The test tag sits on the PlusTextField wrapper; the editable node is the descendant that
        // owns the set-text action.
        test
            .onNode(
                hasSetTextAction() and
                    hasAnyAncestor(hasTestTag(ManageProfileTestTags.DISPLAY_NAME))
            )
            .performTextReplacement(name)
    }

    fun tapSave(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.SAVE) and onScreen).performClick()
    }

    fun assertDisplayed(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.SCREEN)).assertIsDisplayed()
    }
}

fun ComposeUiTest.manageProfile(): ManageProfileRobot = ManageProfileRobot(this)
