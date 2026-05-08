@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.list.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.recipe.list.RecipeListTestTags

/**
 * Robot for interacting with and asserting on the recipe list screen. Lives next to
 * `client/recipe/list/impl` so any test that exercises this screen can compose against the same
 * domain-level vocabulary instead of hardcoding semantics-tree lookups.
 *
 * Every node lookup is scoped to a descendant of [RecipeListTestTags.SCREEN] so a recipe title
 * rendered elsewhere (e.g. on the recipe detail header) doesn't satisfy the matcher.
 *
 * Construct via [recipeList] from inside a `runComposeUiTest { … }` block.
 */
class RecipeListRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(RecipeListTestTags.SCREEN))

    fun assertRecipeIsDisplayed(title: String): RecipeListRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed()
    }

    fun clickRecipe(title: String): RecipeListRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed().performClick()
    }
}

fun ComposeUiTest.recipeList(): RecipeListRobot = RecipeListRobot(this)
