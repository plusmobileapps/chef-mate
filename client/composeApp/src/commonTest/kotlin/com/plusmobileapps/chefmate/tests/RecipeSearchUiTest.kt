package com.plusmobileapps.chefmate.tests

import androidx.compose.ui.test.ExperimentalTestApi
import com.plusmobileapps.chefmate.fixtures.TestRecipes
import com.plusmobileapps.chefmate.harness.TestUserState
import com.plusmobileapps.chefmate.harness.runRootBlocTest
import com.plusmobileapps.chefmate.recipe.list.robots.recipeList
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RecipeSearchUiTest {

    /**
     * A recipe filed under a non-active book is hidden in the default (active-book-scoped) list,
     * but switching the search modal to "All recipe books" surfaces it across every book.
     */
    @Test
    fun searching_all_books_surfaces_recipes_from_other_books() =
        runRootBlocTest(
            userState = TestUserState.Authenticated(recipes = listOf(TestRecipes.fullyPopulated))
        ) { app ->
            val dessertsBookId = app.fakeDatabase.createBook("Desserts")
            app.fakeDatabase.addRecipe(
                TestRecipes.fullyPopulated.copy(
                    id = 99L,
                    title = "Tiramisu",
                    isFavorite = false,
                    recipeBookIds = setOf(dessertsBookId),
                )
            )

            recipeList()
                // Active book only shows its own recipe.
                .assertRecipeIsDisplayed(TestRecipes.fullyPopulated.title)
                .assertRecipeNotDisplayed("Tiramisu")
                // Search across every book surfaces the recipe from the other book.
                .openSearch()
                .selectAllBooks()
                .closeSearch()
                .awaitRecipeDisplayed("Tiramisu")
        }
}
