@file:OptIn(
    kotlin.time.ExperimentalTime::class,
    androidx.compose.ui.test.ExperimentalTestApi::class,
)

package com.plusmobileapps.chefmate.e2e

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.plusmobileapps.chefmate.cook.robots.CookModeRobot
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.list.robots.RecipeListRobot
import com.plusmobileapps.chefmate.testing.E2eTestHarness
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/**
 * Navigation-level end-to-end test. Boots the real DI graph (with only RecipeRemoteDataSource
 * faked), seeds one recipe, renders the App, and walks the user from the recipes tab → recipe
 * detail → cook mode, asserting via [CookModeRobot] that the recipe's ingredients and directions
 * show up.
 */
class CookFlowE2eTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun openingCookModeShowsIngredientsAndDirections() {
        val harness = E2eTestHarness(composeRule)

        runBlocking {
            harness.component.recipeRepository.createRecipe(
                Recipe.Empty.copy(
                    title = "Pasta Carbonara",
                    ingredients = "200g spaghetti\n100g pancetta\n2 eggs",
                    directions = "Boil water\nCook pasta\nMix with eggs",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                )
            )
        }

        harness.launchApp()

        RecipeListRobot(composeRule).openRecipe("Pasta Carbonara")

        // Recipe detail's cook FAB content description comes from
        // recipe_detail_cook_mode = "Cook this recipe".
        composeRule.waitUntil(5_000) {
            composeRule
                .onAllNodes(hasContentDescription("Cook this recipe"))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule.onNodeWithContentDescription("Cook this recipe").performClick()

        CookModeRobot(composeRule)
            .assertRecipeTitleShown("Pasta Carbonara")
            .assertIngredientShown("spaghetti")
            .assertDirectionShown("Boil water")
    }
}
