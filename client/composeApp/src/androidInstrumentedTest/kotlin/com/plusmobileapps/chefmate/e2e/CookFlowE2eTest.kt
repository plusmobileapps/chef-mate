@file:OptIn(
    kotlin.time.ExperimentalTime::class,
    androidx.compose.ui.test.ExperimentalTestApi::class,
)

package com.plusmobileapps.chefmate.e2e

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.plusmobileapps.chefmate.App
import com.plusmobileapps.chefmate.DefaultBlocContext
import com.plusmobileapps.chefmate.cook.robots.CookModeRobot
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.testing.TestAndroidApplicationComponent
import dev.zacsweers.metro.createGraphFactory
import kotlin.time.Clock
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

/**
 * Navigation-level end-to-end test. Boots the real DI graph (with only RecipeRemoteDataSource
 * faked), seeds one recipe, renders [App], and walks the user from the recipes tab → recipe detail
 * → cook mode, asserting via [CookModeRobot] that the recipe's ingredients and directions show up.
 */
class CookFlowE2eTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun openingCookModeShowsIngredientsAndDirections() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Each test gets a fresh DB.
        context.deleteDatabase("chefmate.db")

        val component =
            createGraphFactory<TestAndroidApplicationComponent.Factory>().create(context)

        runBlocking {
            component.recipeRepository.createRecipe(
                Recipe.Empty.copy(
                    title = "Pasta Carbonara",
                    ingredients = "200g spaghetti\n100g pancetta\n2 eggs",
                    directions = "Boil water\nCook pasta\nMix with eggs",
                    createdAt = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                )
            )
        }

        val lifecycle = LifecycleRegistry()
        val rootBloc =
            component.rootBlocFactory.create(
                DefaultBlocContext(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle)
                )
            )
        lifecycle.resume()

        composeRule.setContent { App(rootBloc) }

        // Recipe list is the default landing tab — open the seeded recipe.
        composeRule.waitUntilAtLeastOneExists(hasText("Pasta Carbonara"))
        composeRule.onNodeWithText("Pasta Carbonara").performClick()

        // Recipe detail's cook FAB content description comes from
        // recipe_detail_cook_mode = "Cook this recipe".
        composeRule.waitUntilAtLeastOneExists(hasContentDescription("Cook this recipe"))
        composeRule.onNodeWithContentDescription("Cook this recipe").performClick()

        CookModeRobot(composeRule)
            .assertRecipeTitleShown("Pasta Carbonara")
            .assertIngredientShown("spaghetti")
            .assertDirectionShown("Boil water")
    }
}
