package com.plusmobileapps.chefmate.recipe.core.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailTestTags

/**
 * Robot for interacting with and asserting on the recipe detail screen. Lives next to
 * `client/recipe/core/impl` so any test that exercises this screen can compose against the same
 * domain-level vocabulary instead of hardcoding semantics-tree lookups.
 *
 * Construct via [recipeDetail] from inside a `runComposeUiTest { … }` block.
 */
@OptIn(ExperimentalTestApi::class)
class RecipeDetailRobot(private val test: ComposeUiTest) {

    private val onScreen = hasTestTag(RecipeDetailTestTags.SCREEN)

    fun awaitDisplayed(): RecipeDetailRobot = apply { test.waitUntilExactlyOneExists(onScreen) }

    fun assertRecipeDisplayed(recipeName: String): RecipeDetailRobot = apply {
        test.onNode(hasText(recipeName) and hasAnyAncestor(onScreen)).assertIsDisplayed()
    }

    /** Taps the AI chat action, opening the recipe-grounded chat sheet. */
    fun tapAiChat(): RecipeDetailRobot = apply {
        test
            .onNode(hasTestTag(RecipeDetailTestTags.AI_CHAT_BUTTON) and hasAnyAncestor(onScreen))
            .performClick()
    }
}

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.recipeDetail(): RecipeDetailRobot = RecipeDetailRobot(this)
