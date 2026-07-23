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

    // The picker renders in a sheet/popup outside [RecipeListTestTags.SCREEN], so the matchers
    // below are scoped to the picker's own root tag instead of the screen's.

    private val inBookPicker = hasAnyAncestor(hasTestTag(RecipeListTestTags.BOOK_PICKER))

    fun assertBookListed(name: String): RecipeListRobot = apply {
        test.waitUntilExactlyOneExists(hasText(name) and inBookPicker)
        test.onNode(hasText(name) and inBookPicker).assertIsDisplayed()
    }

    /** Asserts the picker groups collaborators' books under their own "Shared with you" header. */
    fun assertSharedBooksSectionShown(): RecipeListRobot = apply {
        test.onNodeWithTag(RecipeListTestTags.BOOK_PICKER_SHARED_HEADER).assertIsDisplayed()
    }

    fun selectBook(name: String): RecipeListRobot = apply {
        test.onNode(hasText(name) and inBookPicker).performClick()
    }

    fun selectAllRecipes(): RecipeListRobot = apply {
        test.onNodeWithTag(RecipeListTestTags.BOOK_PICKER_ALL_RECIPES).performClick()
    }

    fun tapCreateBook(): RecipeListRobot = apply {
        test.onNodeWithTag(RecipeListTestTags.BOOK_PICKER_CREATE).performClick()
    }
}

fun ComposeUiTest.recipeList(): RecipeListRobot = RecipeListRobot(this)
