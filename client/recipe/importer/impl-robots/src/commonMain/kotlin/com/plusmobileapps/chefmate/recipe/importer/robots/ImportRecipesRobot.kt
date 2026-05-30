@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.importer.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesTestTags

/**
 * Robot for the import recipes screen. Lives next to `client/recipe/importer/impl` so any test that
 * exercises this flow shares the same domain vocabulary instead of hardcoding semantics lookups.
 *
 * Every node lookup is scoped to a descendant of [ImportRecipesTestTags.SCREEN] so titles rendered
 * on other screens don't satisfy the matchers. Construct via [importRecipes] inside a
 * `runComposeUiTest { … }` block.
 */
class ImportRecipesRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(ImportRecipesTestTags.SCREEN))

    fun assertChooseFileVisible(): ImportRecipesRobot = apply {
        test.onNodeWithTag(ImportRecipesTestTags.CHOOSE_FILE_BUTTON).assertIsDisplayed()
    }

    fun assertRecipeDisplayed(title: String): ImportRecipesRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed()
    }

    fun toggleRecipe(title: String): ImportRecipesRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed().performClick()
    }

    fun clickImport(): ImportRecipesRobot = apply {
        test.onNodeWithTag(ImportRecipesTestTags.IMPORT_BUTTON).assertIsDisplayed().performClick()
    }

    fun assertDoneVisible(): ImportRecipesRobot = apply {
        test.onNodeWithTag(ImportRecipesTestTags.DONE_BUTTON).assertIsDisplayed()
    }
}

fun ComposeUiTest.importRecipes(): ImportRecipesRobot = ImportRecipesRobot(this)
