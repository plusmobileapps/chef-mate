package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecipeMultiSelectUiTest {

    @Test
    fun long_press_enters_selection_and_bulk_adds_to_a_category() = runRootBlocTest {
        recipeList()
            .longPressRecipe(TestRecipes.fullyPopulated.title)
            .openSelectionOverflow()
            .tapAddToCategory()
            .assertBulkCategorySheetShown()
            .selectBulkCategory("Dinner")
            // Picking a category files the selection, closes the sheet, and drops back to the list.
            .assertRecipeIsDisplayed(TestRecipes.fullyPopulated.title)
    }

    @Test
    fun selection_overflow_opens_the_add_to_book_picker() = runRootBlocTest {
        recipeList()
            .longPressRecipe(TestRecipes.fullyPopulated.title)
            .openSelectionOverflow()
            .tapAddToBook()
            .assertBulkBookSheetShown()
    }
}
