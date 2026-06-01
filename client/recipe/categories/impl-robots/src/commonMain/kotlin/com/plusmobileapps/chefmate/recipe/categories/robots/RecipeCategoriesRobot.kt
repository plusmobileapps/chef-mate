@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.categories.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesTestTags

/**
 * Robot for the Settings → Recipes → Categories management screen. Every node lookup is scoped
 * under [RecipeCategoriesTestTags.SCREEN] so a row label here never matches a like-named node on
 * another screen (e.g. the same category label rendered in the recipe edit picker sheet).
 */
class RecipeCategoriesRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(RecipeCategoriesTestTags.SCREEN))

    fun awaitDisplayed(): RecipeCategoriesRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(RecipeCategoriesTestTags.SCREEN))
    }

    fun assertCategoryDisplayed(name: String): RecipeCategoriesRobot = apply {
        test.onNode(hasText(name) and onScreen).assertIsDisplayed()
    }

    fun clickCategory(name: String): RecipeCategoriesRobot = apply {
        test.onNode(hasText(name) and onScreen).performClick()
    }

    fun longPressCategory(name: String): RecipeCategoriesRobot = apply {
        test.onNode(hasText(name) and onScreen).performTouchInput { longClick() }
    }
}

fun ComposeUiTest.recipeCategories(): RecipeCategoriesRobot = RecipeCategoriesRobot(this)
