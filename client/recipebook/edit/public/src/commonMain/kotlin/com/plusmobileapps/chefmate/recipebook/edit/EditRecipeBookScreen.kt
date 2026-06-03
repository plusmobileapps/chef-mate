package com.plusmobileapps.chefmate.recipebook.edit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import chefmate.client.recipebook.edit.public.generated.resources.Res
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_name_label
import chefmate.client.recipebook.edit.public.generated.resources.edit_recipe_book_save
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusTextField
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditRecipeBookScreen(bloc: EditRecipeBookBloc, modifier: Modifier = Modifier) {
    val model by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier.testTag(EditRecipeBookTestTags.SCREEN),
        data = PlusHeaderData.Modal(title = model.title, onCloseClick = bloc::onCloseClicked),
    ) {
        PlusTextField(
            value = model.name,
            onValueChange = bloc::onNameChanged,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = ChefMateTheme.dimens.paddingNormal)
                    .testTag(EditRecipeBookTestTags.NAME_FIELD),
            label = { Text(stringResource(Res.string.edit_recipe_book_name_label)) },
            singleLine = true,
            error = model.nameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { bloc.onSaveClicked() }),
        )

        PlusButton(
            text = Res.string.edit_recipe_book_save.asTextData(),
            isLoading = model.isSaving,
            enabled = model.canSave,
            onClick = bloc::onSaveClicked,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(top = ChefMateTheme.dimens.paddingNormal)
                    .testTag(EditRecipeBookTestTags.SAVE_BUTTON),
        )
    }
}
