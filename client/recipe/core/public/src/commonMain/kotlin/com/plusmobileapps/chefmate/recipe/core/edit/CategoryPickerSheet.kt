package com.plusmobileapps.chefmate.recipe.core.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_appetizer
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_breakfast
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_dessert
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_dinner
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_drink
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_lunch
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_other
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_side
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_category_snack
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_create_a11y
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_create_cancel_a11y
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_create_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_delete
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_delete_confirm
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_delete_message
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_delete_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_dialog_cancel
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_more_a11y
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_picker_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_rename
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_rename_confirm
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_rename_placeholder
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_rename_title
import chefmate.client.recipe.core.public.generated.resources.edit_recipe_field_category_save
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Modal-bottom-sheet wrapper around [CategoryPickerContent]. ModalBottomSheet doesn't render
 * reliably under the Compose screenshot test plugin, so screenshot tests target
 * [CategoryPickerContent] directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryPickerSheet(
    selectedCategories: Set<Category>,
    userCategories: List<Category>,
    onAttachBuiltin: (BuiltinCategory) -> Unit,
    onAttachCategory: (Category) -> Unit,
    onDetachCategory: (Category) -> Unit,
    onCreateUserCategory: (String) -> Unit,
    onRenameCategory: (id: Long, newName: String) -> Unit,
    onDeleteCategory: (id: Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // FAB overlays the picker, so reserve enough bottom space that the last list row
            // remains tappable when the user has scrolled to the end.
            CategoryPickerContent(
                selectedCategories = selectedCategories,
                userCategories = userCategories,
                onAttachBuiltin = onAttachBuiltin,
                onAttachCategory = onAttachCategory,
                onDetachCategory = onDetachCategory,
                onCreateUserCategory = onCreateUserCategory,
                onRenameCategory = onRenameCategory,
                onDeleteCategory = onDeleteCategory,
                listBottomPadding = 88.dp,
            )
            ExtendedFloatingActionButton(
                onClick = onDismiss,
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text(stringResource(Res.string.edit_recipe_field_category_save)) },
                modifier =
                    Modifier.align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(ChefMateTheme.dimens.paddingNormal),
            )
        }
    }
}

/**
 * Picker body rendered inside the [CategoryPickerSheet]. Shows preset and user categories with
 * checkboxes; tapping the `+` icon in the header swaps the title row for an inline text field
 * (anchored at the top so it stays visible while the list scrolls under it) for creating a new user
 * category.
 *
 * Public visibility is required so the screenshot-test module (a separate gradle module) can target
 * it directly — ModalBottomSheet doesn't render under the screenshot test plugin.
 */
@Composable
fun CategoryPickerContent(
    selectedCategories: Set<Category>,
    userCategories: List<Category>,
    onAttachBuiltin: (BuiltinCategory) -> Unit,
    onAttachCategory: (Category) -> Unit,
    onDetachCategory: (Category) -> Unit,
    onCreateUserCategory: (String) -> Unit,
    onRenameCategory: (id: Long, newName: String) -> Unit = { _, _ -> },
    onDeleteCategory: (id: Long) -> Unit = {},
    modifier: Modifier = Modifier,
    listBottomPadding: Dp = 0.dp,
) {
    var createState: CategoryCreateState by remember { mutableStateOf(CategoryCreateState.Hidden) }
    var renameTarget by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    val attachedBuiltinIds: Set<String> = selectedCategories.mapNotNull { it.builtinId }.toSet()

    Column(
        modifier = modifier.fillMaxWidth().navigationBarsPadding().imePadding(),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingSmall),
    ) {
        CategoryPickerHeader(
            createState = createState,
            onCreateClicked = {
                if (createState is CategoryCreateState.Hidden) {
                    createState = CategoryCreateState.Editing(text = "")
                }
            },
            onCreateCancelled = { createState = CategoryCreateState.Hidden },
            onCreateTextChanged = { newText ->
                (createState as? CategoryCreateState.Editing)?.let {
                    createState = it.copy(text = newText)
                }
            },
            onCreateSubmit = {
                val current = createState
                if (current is CategoryCreateState.Editing && current.text.isNotBlank()) {
                    // Offline-first: the local insert is effectively synchronous, and the remote
                    // push is fire-and-forget inside the repo, so dismiss the field immediately
                    // rather than waiting on a loading flag. Going through an intermediate
                    // Submitting state used to race with StateFlow conflation and could leave
                    // the spinner stuck on screen.
                    createState = CategoryCreateState.Hidden
                    onCreateUserCategory(current.text)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = ChefMateTheme.dimens.paddingNormal),
            contentPadding = PaddingValues(bottom = listBottomPadding),
        ) {
            items(BuiltinCategory.entries.toList(), key = { "builtin-${it.id}" }) { builtin ->
                val attached = builtin.id in attachedBuiltinIds
                CategoryPickerRow(
                    label = stringResource(builtin.pickerLabelRes()),
                    checked = attached,
                    onCheckedChange = { wantsAttached ->
                        if (wantsAttached) {
                            onAttachBuiltin(builtin)
                        } else {
                            // Find the materialized row for this preset and detach it.
                            selectedCategories
                                .firstOrNull { it.builtinId == builtin.id }
                                ?.let(onDetachCategory)
                        }
                    },
                )
            }

            // `userCategories` from observeUserCategories() also contains materialized preset
            // rows (rows with a non-null builtinId). Filter them out — they're already rendered
            // above by the BuiltinCategory.entries block, and listing them again here would show
            // each attached preset twice once it's been materialized.
            items(userCategories.filter { it.builtinId == null }, key = { "user-${it.id}" }) {
                category ->
                val attached = selectedCategories.any { it.id == category.id }
                CategoryPickerRow(
                    label = category.name,
                    checked = attached,
                    onCheckedChange = { wantsAttached ->
                        if (wantsAttached) onAttachCategory(category)
                        else onDetachCategory(category)
                    },
                    trailing = {
                        UserCategoryOverflowMenu(
                            categoryName = category.name,
                            onRenameClicked = { renameTarget = category },
                            onDeleteClicked = { deleteTarget = category },
                        )
                    },
                )
            }
        }
    }

    renameTarget?.let { target ->
        RenameCategoryDialog(
            initialName = target.name,
            onConfirm = { newName ->
                onRenameCategory(target.id, newName)
                renameTarget = null
            },
            onDismiss = { renameTarget = null },
        )
    }
    deleteTarget?.let { target ->
        DeleteCategoryDialog(
            categoryName = target.name,
            onConfirm = {
                onDeleteCategory(target.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null },
        )
    }
}

@Composable
private fun CategoryPickerHeader(
    createState: CategoryCreateState,
    onCreateClicked: () -> Unit,
    onCreateCancelled: () -> Unit,
    onCreateTextChanged: (String) -> Unit,
    onCreateSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = createState,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = ChefMateTheme.dimens.paddingNormal,
                    end = ChefMateTheme.dimens.paddingSmall,
                ),
        contentKey = { it.headerKey() },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
        when (state) {
            CategoryCreateState.Hidden -> CategoryPickerTitleRow(onCreateClicked = onCreateClicked)
            is CategoryCreateState.Editing ->
                CategoryPickerCreateFieldRow(
                    text = state.text,
                    onTextChanged = onCreateTextChanged,
                    onSubmit = onCreateSubmit,
                    onCancel = onCreateCancelled,
                )
        }
    }
}

@Composable
private fun CategoryPickerTitleRow(onCreateClicked: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.edit_recipe_field_category_picker_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onCreateClicked) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription =
                    stringResource(Res.string.edit_recipe_field_category_create_a11y),
            )
        }
    }
}

@Composable
private fun CategoryPickerCreateFieldRow(
    text: String,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            placeholder = {
                Text(stringResource(Res.string.edit_recipe_field_category_create_placeholder))
            },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
        )
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription =
                    stringResource(Res.string.edit_recipe_field_category_create_cancel_a11y),
            )
        }
    }
}

@Composable
private fun CategoryPickerRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                // Toggleable on the whole row makes the entire surface tappable and merges row +
                // checkbox into a single a11y node announced as "<label>, checkbox, checked/not".
                .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
                .padding(vertical = ChefMateTheme.dimens.paddingExtraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Null onCheckedChange — the parent Row's toggleable handles taps, so the checkbox is
        // purely visual and doesn't intercept the click.
        Checkbox(checked = checked, onCheckedChange = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(start = ChefMateTheme.dimens.paddingSmall),
        )
        // IconButton has its own clickable that consumes taps, so the parent toggleable doesn't
        // fire when the user opens the overflow menu.
        trailing?.invoke()
    }
}

@Composable
private fun UserCategoryOverflowMenu(
    categoryName: String,
    onRenameClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription =
                    stringResource(Res.string.edit_recipe_field_category_more_a11y, categoryName),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.edit_recipe_field_category_rename)) },
                onClick = {
                    expanded = false
                    onRenameClicked()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.edit_recipe_field_category_delete)) },
                onClick = {
                    expanded = false
                    onDeleteClicked()
                },
            )
        }
    }
}

@Composable
private fun RenameCategoryDialog(
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.edit_recipe_field_category_rename_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(stringResource(Res.string.edit_recipe_field_category_rename_placeholder))
                },
                singleLine = true,
                keyboardOptions =
                    KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
                keyboardActions =
                    KeyboardActions(onDone = { if (name.isNotBlank()) onConfirm(name) }),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(Res.string.edit_recipe_field_category_rename_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.edit_recipe_field_category_dialog_cancel))
            }
        },
    )
}

@Composable
private fun DeleteCategoryDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.edit_recipe_field_category_delete_title)) },
        text = {
            Text(stringResource(Res.string.edit_recipe_field_category_delete_message, categoryName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.edit_recipe_field_category_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.edit_recipe_field_category_dialog_cancel))
            }
        },
    )
}

internal sealed interface CategoryCreateState {
    data object Hidden : CategoryCreateState

    data class Editing(val text: String) : CategoryCreateState
}

/**
 * Stable AnimatedContent key that distinguishes the two header layouts but ignores incidental
 * field-text changes — without this every keystroke would re-trigger the cross-fade.
 */
private fun CategoryCreateState.headerKey(): Int =
    when (this) {
        CategoryCreateState.Hidden -> 0
        is CategoryCreateState.Editing -> 1
    }

internal fun BuiltinCategory.pickerLabelRes(): StringResource =
    when (this) {
        BuiltinCategory.BREAKFAST -> Res.string.edit_recipe_category_breakfast
        BuiltinCategory.LUNCH -> Res.string.edit_recipe_category_lunch
        BuiltinCategory.DINNER -> Res.string.edit_recipe_category_dinner
        BuiltinCategory.APPETIZER -> Res.string.edit_recipe_category_appetizer
        BuiltinCategory.SIDE -> Res.string.edit_recipe_category_side
        BuiltinCategory.DESSERT -> Res.string.edit_recipe_category_dessert
        BuiltinCategory.SNACK -> Res.string.edit_recipe_category_snack
        BuiltinCategory.DRINK -> Res.string.edit_recipe_category_drink
        BuiltinCategory.OTHER -> Res.string.edit_recipe_category_other
    }
