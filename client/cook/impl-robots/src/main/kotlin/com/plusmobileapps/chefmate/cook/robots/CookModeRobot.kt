@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.cook.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick

class CookModeRobot(private val rule: ComposeContentTestRule) {

    // The cook screen renders the active recipe in several places (floating header,
    // What's Cooking peek list), so we assert "at least one match is displayed"
    // rather than expecting a unique node.

    fun assertIngredientShown(line: String): CookModeRobot {
        rule.waitUntilTextDisplayed(line)
        rule.onAllNodesWithText(line, substring = true).onFirst().assertIsDisplayed()
        return this
    }

    fun assertDirectionShown(line: String): CookModeRobot {
        rule.waitUntilTextDisplayed(line)
        rule.onAllNodesWithText(line, substring = true).onFirst().assertIsDisplayed()
        return this
    }

    fun assertRecipeTitleShown(title: String): CookModeRobot {
        rule.waitUntilTextDisplayed(title)
        rule.onAllNodesWithText(title, substring = true).onFirst().assertIsDisplayed()
        return this
    }

    fun openWhatsCooking(): WhatsCookingRobot {
        rule.onNodeWithContentDescription(WHATS_COOKING_DESCRIPTION).performClick()
        return WhatsCookingRobot(rule)
    }

    private companion object {
        const val WHATS_COOKING_DESCRIPTION = "What’s Cooking"
    }
}

internal fun ComposeContentTestRule.waitUntilTextDisplayed(text: String, timeoutMs: Long = 5_000) {
    waitUntilAtLeastOneExists(hasText(text, substring = true), timeoutMs)
}

internal fun ComposeContentTestRule.waitUntilAtLeastOneExists(
    matcher: androidx.compose.ui.test.SemanticsMatcher,
    timeoutMs: Long = 5_000,
) {
    waitUntil(timeoutMs) { onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
}
