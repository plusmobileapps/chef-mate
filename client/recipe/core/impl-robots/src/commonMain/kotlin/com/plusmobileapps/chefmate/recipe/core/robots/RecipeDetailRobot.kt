package com.plusmobileapps.chefmate.recipe.core.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
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

    /** Asserts an ingredient line with exactly [text] is on screen (e.g. a scaled amount). */
    fun assertIngredientDisplayed(text: String): RecipeDetailRobot = apply {
        test.onNode(hasText(text) and hasAnyAncestor(onScreen)).assertIsDisplayed()
    }

    /**
     * Opens the ingredients scale dropdown and picks the option with [label] (e.g. `2×`), scaling
     * every ingredient amount by that factor.
     */
    fun selectIngredientScale(label: String): RecipeDetailRobot = apply {
        test
            .onNode(
                hasTestTag(RecipeDetailTestTags.INGREDIENT_SCALE_BUTTON) and
                    hasAnyAncestor(onScreen)
            )
            .performClick()
        test.onNode(hasText(label)).performClick()
    }

    /** Asserts the ingredients scale button currently reads [label] (e.g. `2×`). */
    fun assertIngredientScale(label: String): RecipeDetailRobot = apply {
        test
            .onNode(
                hasTestTag(RecipeDetailTestTags.INGREDIENT_SCALE_BUTTON) and
                    hasAnyAncestor(onScreen)
            )
            .assert(hasText(label))
    }

    /** Taps the add-to-grocery-list action, opening the ingredient-picking sheet. */
    fun tapAddToGroceryList(): RecipeDetailRobot = apply {
        test
            .onNode(
                hasTestTag(RecipeDetailTestTags.ADD_TO_GROCERY_BUTTON) and hasAnyAncestor(onScreen)
            )
            .performClick()
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
