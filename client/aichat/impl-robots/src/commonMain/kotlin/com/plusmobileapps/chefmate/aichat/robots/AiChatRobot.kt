@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.aichat.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.aichat.AiChatTestTags

/**
 * Robot for interacting with and asserting on the AI Chat screen. Every node lookup is scoped under
 * [AiChatTestTags.SCREEN] so headers from other screens don't satisfy matchers.
 */
class AiChatRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(AiChatTestTags.SCREEN))

    /** Waits for the AI chat screen (its header) to be shown. */
    fun awaitShown(): AiChatRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(AiChatTestTags.SCREEN))
    }

    fun typeMessage(text: String): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.INPUT) and onScreen).performTextInput(text)
    }

    fun tapSend(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.SEND_BUTTON) and onScreen).performClick()
    }

    fun tapHistory(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.HISTORY_BUTTON) and onScreen).performClick()
    }

    /** Taps the new-conversation button in the chat's app bar. */
    fun tapNewChat(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.NEW_CHAT_BUTTON) and onScreen).performClick()
    }

    /** Closes the recipe-grounded modal via its app-bar close (X) button. */
    fun close(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.CLOSE_BUTTON) and onScreen).performClick()
    }

    /** Asserts the recipe-grounded modal's app bar shows history, new-conversation, and close. */
    fun assertModalActionsShown(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.HISTORY_BUTTON) and onScreen).assertIsDisplayed()
        test.onNode(hasTestTag(AiChatTestTags.NEW_CHAT_BUTTON) and onScreen).assertIsDisplayed()
        test.onNode(hasTestTag(AiChatTestTags.CLOSE_BUTTON) and onScreen).assertIsDisplayed()
    }

    /** Asserts the attach-photo button (recipe-from-photo entry) is present in the input row. */
    fun assertAttachPhotoShown(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.ATTACH_PHOTO_BUTTON) and onScreen).assertIsDisplayed()
    }

    fun assertMessageShown(text: String): AiChatRobot = apply {
        test.onNode(hasText(text, substring = true) and onScreen).assertIsDisplayed()
    }

    /** Waits for a message containing [text] to appear, then returns. */
    fun awaitMessageShown(text: String): AiChatRobot = apply {
        test.waitUntilExactlyOneExists(hasText(text, substring = true) and onScreen)
    }

    /**
     * Waits for the Add Recipe pill to appear (i.e. at least one finished model reply exists) and
     * then taps it.
     */
    fun tapAddRecipe(): AiChatRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(AiChatTestTags.ADD_RECIPE_PILL) and onScreen)
        test.onNode(hasTestTag(AiChatTestTags.ADD_RECIPE_PILL) and onScreen).performClick()
    }
}

fun ComposeUiTest.aiChat(): AiChatRobot = AiChatRobot(this)
