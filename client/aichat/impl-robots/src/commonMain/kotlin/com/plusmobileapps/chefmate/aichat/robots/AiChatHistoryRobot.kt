@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.aichat.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.aichat.AiChatHistoryTestTags

class AiChatHistoryRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(AiChatHistoryTestTags.SCREEN))

    fun awaitDisplayed(): AiChatHistoryRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(AiChatHistoryTestTags.SCREEN))
    }

    fun assertConversationShown(title: String): AiChatHistoryRobot = apply {
        test.onNode(hasText(title, substring = true) and onScreen).assertIsDisplayed()
    }

    fun tapConversation(conversationId: Long): AiChatHistoryRobot = apply {
        test
            .onNode(
                hasTestTag(AiChatHistoryTestTags.CONVERSATION_ROW_PREFIX + conversationId) and
                    onScreen
            )
            .performClick()
    }

    fun tapNewConversation(): AiChatHistoryRobot = apply {
        test
            .onNode(hasTestTag(AiChatHistoryTestTags.NEW_CONVERSATION_BUTTON) and onScreen)
            .performClick()
    }

    fun tapDeleteConversation(conversationId: Long): AiChatHistoryRobot = apply {
        test
            .onNode(
                hasTestTag(AiChatHistoryTestTags.DELETE_CONVERSATION_PREFIX + conversationId) and
                    onScreen
            )
            .performClick()
    }

    fun tapDeleteAll(): AiChatHistoryRobot = apply {
        test.onNode(hasTestTag(AiChatHistoryTestTags.DELETE_ALL_BUTTON) and onScreen).performClick()
    }
}

fun ComposeUiTest.aiChatHistory(): AiChatHistoryRobot = AiChatHistoryRobot(this)
