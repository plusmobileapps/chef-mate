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
import com.plusmobileapps.chefmate.aichat.AiChatTestTags

/**
 * Robot for interacting with and asserting on the AI Chat screen. Every node lookup is scoped under
 * [AiChatTestTags.SCREEN] so headers from other screens don't satisfy matchers.
 */
class AiChatRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(AiChatTestTags.SCREEN))

    fun typeMessage(text: String): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.INPUT) and onScreen).performTextInput(text)
    }

    fun tapSend(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.SEND_BUTTON) and onScreen).performClick()
    }

    fun tapClear(): AiChatRobot = apply {
        test.onNode(hasTestTag(AiChatTestTags.CLEAR_BUTTON) and onScreen).performClick()
    }

    fun assertMessageShown(text: String): AiChatRobot = apply {
        test.onNode(hasText(text, substring = true) and onScreen).assertIsDisplayed()
    }
}

fun ComposeUiTest.aiChat(): AiChatRobot = AiChatRobot(this)
