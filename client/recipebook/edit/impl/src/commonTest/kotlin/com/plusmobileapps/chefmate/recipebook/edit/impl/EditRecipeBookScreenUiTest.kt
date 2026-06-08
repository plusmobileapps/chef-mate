@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.recipebook.edit.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookBloc
import com.plusmobileapps.chefmate.recipebook.edit.EditRecipeBookScreen
import com.plusmobileapps.chefmate.recipebook.edit.robots.editRecipeBook
import com.plusmobileapps.chefmate.text.FixedString
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow

class EditRecipeBookScreenUiTest {

    @Test
    fun typing_a_name_and_saving_drives_the_bloc() = runComposeUiTest {
        val bloc = RecordingEditBloc()
        setContent { EditRecipeBookScreen(bloc = bloc) }

        editRecipeBook().assertDisplayed().typeName("Grill Nights").save()

        bloc.lastName shouldBe "Grill Nights"
        bloc.saveClicks shouldBe 1
    }

    private class RecordingEditBloc : EditRecipeBookBloc {
        var lastName: String? = null
        var saveClicks: Int = 0

        override val state =
            MutableStateFlow(
                EditRecipeBookBloc.Model(title = FixedString("New recipe book"), name = "x")
            )

        override fun onNameChanged(name: String) {
            lastName = name
            state.value = state.value.copy(name = name)
        }

        override fun onSaveClicked() {
            saveClicks++
        }

        override fun onCloseClicked() = Unit

        @Composable
        override fun Content(modifier: Modifier) {
            EditRecipeBookScreen(bloc = this, modifier = modifier)
        }
    }
}
