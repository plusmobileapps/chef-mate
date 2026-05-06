@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.cook.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.ui.robots.Robot

class CookModeRobot(test: ComposeUiTest) : Robot(test) {

    // Cook mode renders the active recipe in several places at once (floating
    // header, What's Cooking peek list); these helpers therefore assert
    // "at least one match is displayed" rather than expecting a unique node.

    fun assertIngredientShown(line: String) = apply {
        waitUntilTextDisplayed(line)
        test.onAllNodesWithText(line, substring = true).onFirst().assertIsDisplayed()
    }

    fun assertDirectionShown(line: String) = apply {
        waitUntilTextDisplayed(line)
        test.onAllNodesWithText(line, substring = true).onFirst().assertIsDisplayed()
    }

    fun assertRecipeTitleShown(title: String) = apply {
        waitUntilTextDisplayed(title)
        test.onAllNodesWithText(title, substring = true).onFirst().assertIsDisplayed()
    }

    fun openWhatsCooking(): WhatsCookingRobot {
        test.onNodeWithContentDescription(WHATS_COOKING_DESCRIPTION).performClick()
        return WhatsCookingRobot(test)
    }

    private companion object {
        const val WHATS_COOKING_DESCRIPTION = "What’s Cooking"
    }
}
