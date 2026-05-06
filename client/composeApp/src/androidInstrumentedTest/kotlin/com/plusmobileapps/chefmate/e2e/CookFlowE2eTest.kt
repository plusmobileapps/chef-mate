@file:OptIn(
    kotlin.time.ExperimentalTime::class,
    androidx.compose.ui.test.ExperimentalTestApi::class,
)

package com.plusmobileapps.chefmate.e2e

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.plusmobileapps.chefmate.cook.robots.CookModeRobot
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.list.robots.RecipeListRobot
import com.plusmobileapps.chefmate.testing.E2eTestHarness
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking
import org.junit.Test

/**
 * Navigation-level end-to-end test. Boots the real DI graph (with only RecipeRemoteDataSource
 * faked), seeds one recipe, renders the App, and walks the user from the recipes tab → recipe
 * detail → cook mode, asserting via [CookModeRobot] that the recipe's ingredients and directions
 * show up.
 */
class CookFlowE2eTest {

    @Test
    fun openingCookModeShowsIngredientsAndDirections() = runComposeUiTest {
        val harness = E2eTestHarness(this)

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

        RecipeListRobot(this).openRecipe("Pasta Carbonara")

        // Recipe detail's cook FAB content description comes from
        // recipe_detail_cook_mode = "Cook this recipe".
        waitUntil(timeoutMillis = 5_000) {
            onAllNodes(hasContentDescription("Cook this recipe")).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithContentDescription("Cook this recipe").performClick()

        CookModeRobot(this)
            .assertRecipeTitleShown("Pasta Carbonara")
            .assertIngredientShown("spaghetti")
            .assertDirectionShown("Boil water")
    }
}
