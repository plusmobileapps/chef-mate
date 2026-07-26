package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.core.robots.recipeDetail
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecipeScaleIngredientsUiTest {

    @Test
    fun scaling_ingredients_doubles_the_displayed_amounts() = runRootBlocTest {
        // TestRecipes.fullyPopulated starts with "200g spaghetti" and "2 large eggs".
        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail()
            .awaitDisplayed()
            .assertIngredientDisplayed("200g spaghetti")
            .selectIngredientScale("2×")
            .assertIngredientScale("2×")
            .assertIngredientDisplayed("400g spaghetti")
    }
}
