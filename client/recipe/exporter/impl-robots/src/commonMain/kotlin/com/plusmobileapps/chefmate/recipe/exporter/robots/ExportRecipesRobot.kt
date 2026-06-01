@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.exporter.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesTestTags

/**
 * Robot for the export recipes screen. Lives next to `client/recipe/exporter/impl` so any test that
 * exercises this flow shares the same domain vocabulary instead of hardcoding semantics lookups.
 *
 * Every node lookup is scoped to a descendant of [ExportRecipesTestTags.SCREEN] so titles rendered
 * on other screens don't satisfy matchers. Construct via [exportRecipes] inside a `runComposeUiTest
 * { … }` block.
 */
class ExportRecipesRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(ExportRecipesTestTags.SCREEN))

    fun assertRecipeDisplayed(title: String): ExportRecipesRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed()
    }

    fun toggleRecipe(title: String): ExportRecipesRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed().performClick()
    }

    fun clickExport(): ExportRecipesRobot = apply {
        test.onNodeWithTag(ExportRecipesTestTags.EXPORT_BUTTON).assertIsDisplayed().performClick()
    }

    fun assertDoneVisible(): ExportRecipesRobot = apply {
        test.onNodeWithTag(ExportRecipesTestTags.DONE_BUTTON).assertIsDisplayed()
    }
}

fun ComposeUiTest.exportRecipes(): ExportRecipesRobot = ExportRecipesRobot(this)
