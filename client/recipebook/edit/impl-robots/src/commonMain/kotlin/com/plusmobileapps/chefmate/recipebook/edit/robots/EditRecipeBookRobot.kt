@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipebook.edit.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookTestTags

class EditRecipeBookRobot(private val test: ComposeUiTest) {

    fun assertDisplayed(): EditRecipeBookRobot = apply {
        test.onNodeWithTag(EditRecipeBookTestTags.SCREEN).assertIsDisplayed()
    }

    fun typeName(name: String): EditRecipeBookRobot = apply {
        // The test tag sits on the PlusTextField wrapper; the editable node is the inner field.
        test
            .onNode(
                hasSetTextAction() and hasAnyAncestor(hasTestTag(EditRecipeBookTestTags.NAME_FIELD))
            )
            .performTextReplacement(name)
    }

    fun save(): EditRecipeBookRobot = apply {
        test.onNodeWithTag(EditRecipeBookTestTags.SAVE_BUTTON).performClick()
    }
}

fun ComposeUiTest.editRecipeBook(): EditRecipeBookRobot = EditRecipeBookRobot(this)
