package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.core.robots.recipeDetail
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RootNavigationUiTest {

    @Test
    fun clicking_recipe_in_list_navigates_to_recipe_detail() = runRootBlocTest {
        recipeList().clickRecipe(TestRecipes.fullyPopulated.title)
        recipeDetail().awaitDisplayed().assertRecipeDisplayed(TestRecipes.fullyPopulated.title)
    }
}
