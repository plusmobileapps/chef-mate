package com.plusmobileapps.chefmate.cook.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.cook.WhatsCookingTestTags
import com.plusmobileapps.chefmate.ui.robots.Robot

class WhatsCookingRobot(rule: ComposeContentTestRule) : Robot(rule) {

    fun assertRecipeListed(title: String) = apply {
        waitUntilTextDisplayed(title)
        rule.onNodeWithText(title).assertIsDisplayed()
    }

    fun selectRecipe(recipeId: Long) = apply {
        rule.onNodeWithTag(WhatsCookingTestTags.recipeRow(recipeId)).performClick()
    }

    fun toggleSelectMode() = apply {
        rule.onNodeWithTag(WhatsCookingTestTags.SelectModeToggle).performClick()
    }
}
