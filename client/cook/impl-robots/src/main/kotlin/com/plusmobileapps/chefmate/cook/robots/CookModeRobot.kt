package com.plusmobileapps.chefmate.cook.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.ui.robots.Robot

class CookModeRobot(rule: ComposeContentTestRule) : Robot(rule) {

    // Cook mode renders the active recipe in several places at once (floating
    // header, What's Cooking peek list); these helpers therefore assert
    // "at least one match is displayed" rather than expecting a unique node.

    fun assertIngredientShown(line: String) = apply {
        waitUntilTextDisplayed(line)
        rule.onAllNodesWithText(line, substring = true).onFirst().assertIsDisplayed()
    }

    fun assertDirectionShown(line: String) = apply {
        waitUntilTextDisplayed(line)
        rule.onAllNodesWithText(line, substring = true).onFirst().assertIsDisplayed()
    }

    fun assertRecipeTitleShown(title: String) = apply {
        waitUntilTextDisplayed(title)
        rule.onAllNodesWithText(title, substring = true).onFirst().assertIsDisplayed()
    }

    fun openWhatsCooking(): WhatsCookingRobot {
        rule.onNodeWithContentDescription(WHATS_COOKING_DESCRIPTION).performClick()
        return WhatsCookingRobot(rule)
    }

    private companion object {
        const val WHATS_COOKING_DESCRIPTION = "What’s Cooking"
    }
}
