package com.plusmobileapps.chefmate.recipe.list.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.ui.robots.Robot

class RecipeListRobot(rule: ComposeContentTestRule) : Robot(rule) {

    fun assertRecipeShown(title: String) = apply {
        waitUntilTextDisplayed(title)
        rule.onAllNodesWithText(title, substring = true).onFirst().assertIsDisplayed()
    }

    fun openRecipe(title: String) = apply {
        waitUntilTextDisplayed(title)
        rule.onAllNodesWithText(title, substring = true).onFirst().performClick()
    }
}
