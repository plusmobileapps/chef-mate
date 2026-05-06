@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.list.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.ui.robots.Robot

class RecipeListRobot(test: ComposeUiTest) : Robot(test) {

    fun assertRecipeShown(title: String) = apply {
        waitUntilTextDisplayed(title)
        test.onAllNodesWithText(title, substring = true).onFirst().assertIsDisplayed()
    }

    fun openRecipe(title: String) = apply {
        waitUntilTextDisplayed(title)
        test.onAllNodesWithText(title, substring = true).onFirst().performClick()
    }
}
