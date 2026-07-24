@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.aichat.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
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
    private val onPeek = hasAnyAncestor(hasTestTag(AiChatTestTags.PEEK))

    fun typeMessage(text: String): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.INPUT) and onScreen).performTextInput(text)
    }

    /** Waits for the collapsed sheet "peek" (input over the recipe, no header) to appear. */
    fun awaitPeekShown(): AiChatRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(AiChatTestTags.PEEK))
    }

    /** Types into the peek's input. The peek stays collapsed while typing — it does not expand. */
    fun typeInPeek(text: String): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.INPUT) and onPeek).performTextInput(text)
    }

    /**
     * Asserts the collapsed peek is still shown (i.e. typing/focusing did not expand the sheet).
     */
    fun assertStillPeek(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.PEEK)).assertIsDisplayed()
    }

    /** Asserts the peek's action bar shows both the history and close buttons. */
    fun assertPeekActionsShown(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.HISTORY_BUTTON) and onPeek).assertIsDisplayed()
        test.onNode(hasTestTag(AiChatTestTags.CLOSE_BUTTON) and onPeek).assertIsDisplayed()
    }

    /**
     * Asserts the peek's input is focused (opening the sheet drops the user straight into typing).
     */
    fun assertPeekInputFocused(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.INPUT) and onPeek).assertIsFocused()
    }

    /** Taps the peek's send button, which sends the message and then expands the sheet. */
    fun sendFromPeek(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.SEND_BUTTON) and onPeek).performClick()
    }

    /** Taps the peek's new-conversation button. */
    fun tapNewChatFromPeek(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.NEW_CHAT_BUTTON) and onPeek).performClick()
    }

    /** Waits for the expanded, full-screen chat (its header) to be shown. */
    fun awaitExpanded(): AiChatRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(AiChatTestTags.SCREEN))
    }

    /** Closes the expanded sheet via its app-bar close (X) button. */
    fun closeFromExpanded(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.CLOSE_BUTTON) and onScreen).performClick()
    }

    /** Waits for the peek's conversation excerpt (shown when the recipe already has a chat). */
    fun awaitPeekExcerptShown(): AiChatRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(AiChatTestTags.PEEK_EXCERPT))
    }

    /** Asserts the peek (its excerpt) shows text containing [text]. */
    fun assertPeekShowsText(text: String): AiChatRobot = apply {
        test.onNode(hasText(text, substring = true) and onPeek).assertIsDisplayed()
    }

    fun tapSend(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.SEND_BUTTON) and onScreen).performClick()
    }

    fun tapHistory(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.HISTORY_BUTTON) and onScreen).performClick()
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
