package com.plusmobileapps.chefmate.recipe.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import chefmate.client.recipe.categories.public.generated.resources.Res
import chefmate.client.recipe.categories.public.generated.resources.category_delete_confirm
import chefmate.client.recipe.categories.public.generated.resources.category_delete_message
import chefmate.client.recipe.categories.public.generated.resources.category_delete_title
import chefmate.client.recipe.categories.public.generated.resources.category_dialog_cancel
import chefmate.client.recipe.categories.public.generated.resources.category_rename_confirm
import chefmate.client.recipe.categories.public.generated.resources.category_rename_placeholder
import chefmate.client.recipe.categories.public.generated.resources.category_rename_title
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusDialogScaffold
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun RenameCategoryDialog(initialName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    PlusDialogScaffold(
        onDismissRequest = onDismiss,
        header = { Text(stringResource(Res.string.category_rename_title)) },
        content = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text(stringResource(Res.string.category_rename_placeholder)) },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
                keyboardActions =
                    KeyboardActions(onDone = { if (name.isNotBlank()) onConfirm(name) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        },
        footer = {
            Row(horizontalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingNormal)) {
                PlusButton(
                    text = ResourceString(Res.string.category_dialog_cancel),
                    variant = PlusButtonVariant.SECONDARY,
                    onClick = onDismiss,
                )
                PlusButton(
                    text = ResourceString(Res.string.category_rename_confirm),
                    enabled = name.isNotBlank(),
                    onClick = { onConfirm(name) },
                )
            }
        },
    )
}

@Composable
fun DeleteCategoryDialog(categoryName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    PlusDialog(
        title = ResourceString(Res.string.category_delete_title),
        message =
            PhraseModel(
                Res.string.category_delete_message,
                "category" to FixedString(categoryName),
            ),
        confirmButtonText = ResourceString(Res.string.category_delete_confirm),
        dismissButtonText = ResourceString(Res.string.category_dialog_cancel),
        onConfirmClick = onConfirm,
        onDismissRequest = onDismiss,
    )
}
