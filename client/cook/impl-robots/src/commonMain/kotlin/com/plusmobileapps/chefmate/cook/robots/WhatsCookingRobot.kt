@file:OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.cook.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.cook.WhatsCookingTestTags
import com.plusmobileapps.chefmate.ui.robots.Robot

class WhatsCookingRobot(test: ComposeUiTest) : Robot(test) {

    fun assertRecipeListed(title: String) = apply {
        waitUntilTextDisplayed(title)
        test.onNodeWithText(title).assertIsDisplayed()
    }

    fun selectRecipe(recipeId: Long) = apply {
        test.onNodeWithTag(WhatsCookingTestTags.recipeRow(recipeId)).performClick()
    }

    fun toggleSelectMode() = apply {
        test.onNodeWithTag(WhatsCookingTestTags.SelectModeToggle).performClick()
    }
}
