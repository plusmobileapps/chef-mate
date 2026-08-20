@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.family.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.family.core.FamilyTestTags

class FamilyRobot(private val test: ComposeUiTest) {

    /** The family state loads asynchronously, so wait for the screen before asserting on it. */
    fun awaitDisplayed(): FamilyRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(FamilyTestTags.SCREEN))
    }

    fun assertDisplayed(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.SCREEN).assertIsDisplayed()
    }

    /** With no family yet, the screen shows the create form rather than a member list. */
    fun assertCreateFormShown(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.CREATE_BUTTON).assertIsDisplayed()
    }

    fun typeFamilyName(name: String): FamilyRobot = apply {
        // The test tag sits on the PlusTextField wrapper; the editable node is the inner field.
        test
            .onNode(
                hasSetTextAction() and hasAnyAncestor(hasTestTag(FamilyTestTags.CREATE_NAME_FIELD))
            )
            .performTextReplacement(name)
    }

    fun createFamily(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.CREATE_BUTTON).performClick()
    }

    fun typeInviteEmail(email: String): FamilyRobot = apply {
        test
            .onNode(
                hasSetTextAction() and hasAnyAncestor(hasTestTag(FamilyTestTags.INVITE_EMAIL_FIELD))
            )
            .performTextReplacement(email)
    }

    fun sendInvite(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.INVITE_BUTTON).performClick()
    }

    fun assertMembersShown(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.MEMBERS).assertIsDisplayed()
    }

    /** The invite controls are owner-only, so this asserts the current user is not the owner. */
    fun assertCannotInvite(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.INVITE_BUTTON).assertDoesNotExist()
    }

    fun leaveFamily(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.LEAVE_BUTTON).performClick()
    }

    fun deleteFamily(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.DELETE_BUTTON).performClick()
    }

    fun assertDeleteNotShown(): FamilyRobot = apply {
        test.onNodeWithTag(FamilyTestTags.DELETE_BUTTON).assertDoesNotExist()
    }

    /** Taps the confirm button in a leave/delete/remove dialog. */
    fun confirmDialog(text: String): FamilyRobot = apply {
        test.onNodeWithText(text).performClick()
    }
}

fun ComposeUiTest.family(): FamilyRobot = FamilyRobot(this)
