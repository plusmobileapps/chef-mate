package com.plusmobileapps.chefmate.cook.robots

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.cook.WhatsCookingTestTags

class WhatsCookingRobot(private val rule: ComposeContentTestRule) {

    fun assertRecipeListed(title: String): WhatsCookingRobot {
        rule.waitUntilTextDisplayed(title)
        rule.onNodeWithText(title).assertIsDisplayed()
        return this
    }

    fun selectRecipe(recipeId: Long): WhatsCookingRobot {
        rule.onNodeWithTag(WhatsCookingTestTags.recipeRow(recipeId)).performClick()
        return this
    }

    fun toggleSelectMode(): WhatsCookingRobot {
        rule.onNodeWithTag(WhatsCookingTestTags.SelectModeToggle).performClick()
        return this
    }
}
