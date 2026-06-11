@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.list.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.waitUntilExactlyOneExists
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

    /** Waits for a recipe with [title] to appear in the list (e.g. after an async scope change). */
    fun awaitRecipeDisplayed(title: String): RecipeListRobot = apply {
        test.waitUntilExactlyOneExists(hasText(title) and onScreen)
    }

    fun assertRecipeNotDisplayed(title: String): RecipeListRobot = apply {
        test.onNode(hasText(title) and onScreen).assertDoesNotExist()
    }

    fun clickRecipe(title: String): RecipeListRobot = apply {
        test.onNode(hasText(title) and onScreen).assertIsDisplayed().performClick()
    }

    /** Opens the "+" add-recipe chooser menu. */
    fun openAddMenu(): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.ADD_RECIPE_BUTTON) and onScreen).performClick()
    }

    /**
     * Asserts the chooser menu shows the "Scan from photo" entry. The menu renders in a popup
     * outside [RecipeListTestTags.SCREEN], so matchers here are not ancestor-scoped.
     */
    fun assertScanFromPhotoShown(): RecipeListRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(RecipeListTestTags.ADD_MENU_SCAN))
        test.onNode(hasTestTag(RecipeListTestTags.ADD_MENU_SCAN)).assertIsDisplayed()
    }

    /** Taps "Create recipe" in the chooser menu. */
    fun tapCreateRecipe(): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.ADD_MENU_CREATE)).performClick()
    }

    /** Taps "Scan from photo" in the chooser menu (launches the platform image picker). */
    fun tapScanFromPhoto(): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.ADD_MENU_SCAN)).performClick()
    }

    fun assertActiveBook(name: String): RecipeListRobot = apply {
        test
            .onNode(hasText(name) and hasTestTag(RecipeListTestTags.BOOK_SELECTOR))
            .assertIsDisplayed()
    }

    fun openBookPicker(): RecipeListRobot = apply {
        test.onNodeWithTag(RecipeListTestTags.BOOK_SELECTOR).performClick()
    }

    /** Opens the search modal from the app-bar Search action. */
    fun openSearch(): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.SEARCH_BUTTON) and onScreen).performClick()
        // The sheet renders in a popup outside SCREEN; wait for it before interacting.
        test.waitUntilExactlyOneExists(hasTestTag(RecipeListTestTags.SEARCH_SHEET))
    }

    /** Types into the search field of the open modal. */
    fun typeSearch(query: String): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.SEARCH_FIELD)).performTextInput(query)
    }

    /** Selects the "All recipe books" scope in the open search modal. */
    fun selectAllBooks(): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.SEARCH_SCOPE_ALL)).performClick()
    }

    /** Dismisses the search modal via its Done button. */
    fun closeSearch(): RecipeListRobot = apply {
        test.onNode(hasTestTag(RecipeListTestTags.SEARCH_DONE)).performClick()
    }
}

fun ComposeUiTest.recipeList(): RecipeListRobot = RecipeListRobot(this)
