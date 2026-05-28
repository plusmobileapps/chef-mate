@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipe.core.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeTestTags

/**
 * Robot for the recipe edit / create form. Every node lookup is scoped under
 * [EditRecipeTestTags.SCREEN] so the screen's text matches don't collide with anything left behind
 * from the previous route.
 */
class EditRecipeRobot(private val test: ComposeUiTest) {

    private val onScreen = hasAnyAncestor(hasTestTag(EditRecipeTestTags.SCREEN))

    fun awaitDisplayed(): EditRecipeRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(EditRecipeTestTags.SCREEN))
    }

    fun assertTitleShown(title: String): EditRecipeRobot = apply {
        test.waitUntilExactlyOneExists(hasText(title) and onScreen)
        test.onNode(hasText(title) and onScreen).assertIsDisplayed()
    }
}

fun ComposeUiTest.editRecipe(): EditRecipeRobot = EditRecipeRobot(this)
