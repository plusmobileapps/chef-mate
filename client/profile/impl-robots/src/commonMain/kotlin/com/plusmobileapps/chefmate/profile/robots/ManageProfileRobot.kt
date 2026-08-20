@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.profile.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isEnabled
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

    fun setHandle(handle: String): ManageProfileRobot = apply {
        test
            .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(ManageProfileTestTags.HANDLE)))
            .performTextReplacement(handle)
    }

    fun setBio(bio: String): ManageProfileRobot = apply {
        test
            .onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(ManageProfileTestTags.BIO)))
            .performTextReplacement(bio)
    }

    /**
     * Waits for Save to become enabled. Claiming a handle debounces an availability check before
     * the button unlocks, so a test that types a handle and taps Save immediately would race it.
     */
    fun awaitSaveEnabled(): ManageProfileRobot = apply {
        test.waitUntilExactlyOneExists(
            hasTestTag(ManageProfileTestTags.SAVE) and onScreen and isEnabled()
        )
    }

    fun assertSaveEnabled(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.SAVE) and onScreen).assertIsEnabled()
    }

    fun assertSaveDisabled(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.SAVE) and onScreen).assertIsNotEnabled()
    }

    fun tapSave(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.SAVE) and onScreen).performClick()
    }

    fun tapDeleteAccount(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.DELETE_ACCOUNT) and onScreen).performClick()
    }

    fun typeDeleteConfirmation(email: String): ManageProfileRobot = apply {
        // The test tag sits on the PlusTextField wrapper; the editable node is the descendant that
        // owns the set-text action.
        test
            .onNode(
                hasSetTextAction() and
                    hasAnyAncestor(hasTestTag(ManageProfileTestTags.DELETE_CONFIRMATION))
            )
            .performTextReplacement(email)
    }

    fun tapConfirmDelete(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.DELETE_CONFIRM)).performClick()
    }

    fun assertConfirmDeleteEnabled(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.DELETE_CONFIRM)).assertIsEnabled()
    }

    fun assertConfirmDeleteDisabled(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.DELETE_CONFIRM)).assertIsNotEnabled()
    }

    fun assertDisplayed(): ManageProfileRobot = apply {
        test.onNode(hasTestTag(ManageProfileTestTags.SCREEN)).assertIsDisplayed()
    }
}

fun ComposeUiTest.manageProfile(): ManageProfileRobot = ManageProfileRobot(this)
