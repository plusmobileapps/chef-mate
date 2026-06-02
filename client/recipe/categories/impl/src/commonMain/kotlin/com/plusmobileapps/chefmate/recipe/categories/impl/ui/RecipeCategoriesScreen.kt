package com.plusmobileapps.chefmate.recipe.categories.impl.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.categories.public.generated.resources.Res
import chefmate.client.recipe.categories.public.generated.resources.category_delete
import chefmate.client.recipe.categories.public.generated.resources.category_more_a11y
import chefmate.client.recipe.categories.public.generated.resources.category_rename
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_back
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_built_in_label
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_bulk_delete_message_one
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_bulk_delete_message_other
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_bulk_delete_title
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_create_a11y
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_create_cancel_a11y
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_create_placeholder
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_empty_user
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_recipe_count_one
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_recipe_count_other
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_recipe_count_zero
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_section_builtin
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_section_user
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_select_mode_a11y
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_selection_count
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_selection_delete_a11y
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_selection_empty_title
import chefmate.client.recipe.categories.public.generated.resources.recipe_categories_title
import com.plusmobileapps.chefmate.recipe.categories.DeleteCategoryDialog
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CategoryItem
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.CreateState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesBloc.DialogState
import com.plusmobileapps.chefmate.recipe.categories.RecipeCategoriesTestTags
import com.plusmobileapps.chefmate.recipe.categories.RenameCategoryDialog
import com.plusmobileapps.chefmate.recipe.categories.pickerLabelRes
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainerDefaults
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeCategoriesScreen(bloc: RecipeCategoriesBloc, modifier: Modifier = Modifier) {
    val model by bloc.state.collectAsState()
    RecipeCategoriesContent(model = model, handlers = blocHandlers(bloc), modifier = modifier)
}

internal data class RecipeCategoriesHandlers(
    val onBackClicked: () -> Unit,
    val onCategoryClicked: (CategoryItem) -> Unit,
    val onCategoryLongClicked: (CategoryItem) -> Unit,
    val onSelectModeClicked: () -> Unit,
    val onCancelSelection: () -> Unit,
    val onCreateClicked: () -> Unit,
    val onCreateCancelled: () -> Unit,
    val onCreateTextChanged: (String) -> Unit,
    val onCreateSubmitted: () -> Unit,
    val onRenameRequested: (CategoryItem) -> Unit,
    val onRenameSubmitted: (Long, String) -> Unit,
    val onRenameDismissed: () -> Unit,
    val onDeleteRequested: (CategoryItem) -> Unit,
    val onDeleteConfirmed: () -> Unit,
    val onDeleteDismissed: () -> Unit,
    val onBulkDeleteRequested: () -> Unit,
    val onBulkDeleteConfirmed: () -> Unit,
    val onBulkDeleteDismissed: () -> Unit,
)

private fun blocHandlers(bloc: RecipeCategoriesBloc): RecipeCategoriesHandlers =
    RecipeCategoriesHandlers(
        onBackClicked = bloc::onBackClicked,
        onCategoryClicked = bloc::onCategoryClicked,
        onCategoryLongClicked = bloc::onCategoryLongClicked,
        onSelectModeClicked = bloc::onSelectModeClicked,
        onCancelSelection = bloc::onCancelSelection,
        onCreateClicked = bloc::onCreateClicked,
        onCreateCancelled = bloc::onCreateCancelled,
        onCreateTextChanged = bloc::onCreateTextChanged,
        onCreateSubmitted = bloc::onCreateSubmitted,
        onRenameRequested = bloc::onRenameRequested,
        onRenameSubmitted = bloc::onRenameSubmitted,
        onRenameDismissed = bloc::onRenameDismissed,
        onDeleteRequested = bloc::onDeleteRequested,
        onDeleteConfirmed = bloc::onDeleteConfirmed,
        onDeleteDismissed = bloc::onDeleteDismissed,
        onBulkDeleteRequested = bloc::onBulkDeleteRequested,
        onBulkDeleteConfirmed = bloc::onBulkDeleteConfirmed,
        onBulkDeleteDismissed = bloc::onBulkDeleteDismissed,
    )

@Composable
internal fun RecipeCategoriesContent(
    model: RecipeCategoriesBloc.Model,
    handlers: RecipeCategoriesHandlers,
    modifier: Modifier = Modifier,
) {
    val headerData =
        if (model.selectionMode) {
            PlusHeaderData.Modal(
                title =
                    if (model.selectedCount == 0)
                        Res.string.recipe_categories_selection_empty_title.asTextData()
                    else
                        PhraseModel(
                            Res.string.recipe_categories_selection_count,
                            "count" to FixedString(model.selectedCount.toString()),
                        ),
                onCloseClick = handlers.onCancelSelection,
                // Hide the trash until at least one row is selected — a disabled-looking icon
                // without feedback is more confusing than nothing.
                trailingAccessory =
                    if (model.selectedCount == 0) null
                    else
                        PlusHeaderData.TrailingAccessory.Icon(
                            icon = Icons.Default.Delete,
                            contentDesc =
                                Res.string.recipe_categories_selection_delete_a11y.asTextData(),
                            onClick = handlers.onBulkDeleteRequested,
                        ),
            )
        } else {
            PlusHeaderData.Child(
                title = Res.string.recipe_categories_title.asTextData(),
                onBackClick = handlers.onBackClicked,
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Icon(
                        icon = Icons.Default.Checklist,
                        contentDesc = Res.string.recipe_categories_select_mode_a11y.asTextData(),
                        onClick = handlers.onSelectModeClicked,
                    ),
            )
        }

    PlusHeaderContainer(
        modifier = modifier.testTag(RecipeCategoriesTestTags.SCREEN),
        data = headerData,
        // Disable the container's outer vertical scroll so the inner LazyColumn (which itself
        // scrolls) doesn't sit under infinite-height constraints — Compose throws on that nesting.
        scrollEnabled = false,
        // Let the LazyColumn span the full window width so the scroll wheel responds anywhere on
        // wide windows. Individual rows still cap themselves at MaxContentWidth (see CategoryRow /
        // CreateFieldRow) so content doesn't stretch edge-to-edge.
        maxContentWidth = Dp.Unspecified,
        horizontalAlignment = Alignment.CenterHorizontally,
        content = {
            CreateFieldRow(
                createState = model.createState,
                onTextChanged = handlers.onCreateTextChanged,
                onSubmit = handlers.onCreateSubmitted,
                onCancel = handlers.onCreateCancelled,
            )
            CategoryList(
                items = model.categories,
                selectedIds = model.selectedIds,
                selectionMode = model.selectionMode,
                createOpen = model.createState is CreateState.Editing,
                onCategoryClicked = handlers.onCategoryClicked,
                onCategoryLongClicked = handlers.onCategoryLongClicked,
                onRenameRequested = handlers.onRenameRequested,
                onDeleteRequested = handlers.onDeleteRequested,
                onCreateClicked = handlers.onCreateClicked,
            )
        },
    )

    when (val dialog = model.dialog) {
        DialogState.None -> Unit
        is DialogState.Rename ->
            RenameCategoryDialog(
                initialName = dialog.target.name,
                onConfirm = { newName -> handlers.onRenameSubmitted(dialog.target.id, newName) },
                onDismiss = handlers.onRenameDismissed,
            )
        is DialogState.Delete ->
            DeleteCategoryDialog(
                categoryName = dialog.target.name,
                onConfirm = handlers.onDeleteConfirmed,
                onDismiss = handlers.onDeleteDismissed,
            )
        is DialogState.BulkDelete ->
            PlusDialog(
                title = ResourceString(Res.string.recipe_categories_bulk_delete_title),
                message =
                    if (dialog.count == 1)
                        ResourceString(Res.string.recipe_categories_bulk_delete_message_one)
                    else
                        PhraseModel(
                            Res.string.recipe_categories_bulk_delete_message_other,
                            "count" to FixedString(dialog.count.toString()),
                        ),
                confirmButtonText = ResourceString(Res.string.category_delete),
                dismissButtonText = ResourceString(Res.string.recipe_categories_back),
                onConfirmClick = handlers.onBulkDeleteConfirmed,
                onDismissRequest = handlers.onBulkDeleteDismissed,
            )
    }
}

@Composable
private fun CreateFieldRow(
    createState: CreateState,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    AnimatedContent(
        targetState = createState,
        // Cap the AnimatedContent itself rather than the inner Row — the column's
        // `horizontalAlignment = CenterHorizontally` centers AnimatedContent based on its own
        // measured width, not its children's.
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth).fillMaxWidth(),
        // Animate only on the Hidden ↔ Editing flip — without a stable key the outgoing slot
        // would re-derive its state from the outer composition after the transition starts and
        // crash trying to cast Hidden to Editing.
        contentKey = { it is CreateState.Editing },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
        if (state is CreateState.Editing) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            val text = state.text
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            start = ChefMateTheme.dimens.paddingNormal,
                            end = ChefMateTheme.dimens.paddingSmall,
                        )
                        .heightIn(min = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(stringResource(Res.string.recipe_categories_create_placeholder))
                    },
                    singleLine = true,
                    keyboardOptions =
                        KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier =
                        Modifier.weight(1f)
                            .focusRequester(focusRequester)
                            .testTag(RecipeCategoriesTestTags.CREATE_FIELD),
                )
                IconButton(onClick = { if (text.isNotBlank()) onSubmit() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription =
                            stringResource(Res.string.recipe_categories_create_a11y),
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription =
                            stringResource(Res.string.recipe_categories_create_cancel_a11y),
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth())
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryList(
    items: List<CategoryItem>,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    createOpen: Boolean,
    onCategoryClicked: (CategoryItem) -> Unit,
    onCategoryLongClicked: (CategoryItem) -> Unit,
    onRenameRequested: (CategoryItem) -> Unit,
    onDeleteRequested: (CategoryItem) -> Unit,
    onCreateClicked: () -> Unit,
) {
    // Partition once per render; the upstream flow already sorts by name, and stable LazyColumn
    // keys mean reorder-on-create animates cleanly.
    val (userItems, builtinItems) = items.partition { !it.isBuiltin }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        stickyHeader(key = "header-user") {
            SectionHeader(
                title = stringResource(Res.string.recipe_categories_section_user),
                trailing = {
                    // Hide the create affordance during selection mode (the trash icon in the bar
                    // owns destructive flow) and while the inline create field is already open.
                    if (!selectionMode && !createOpen) {
                        IconButton(onClick = onCreateClicked) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription =
                                    stringResource(Res.string.recipe_categories_create_a11y),
                            )
                        }
                    }
                },
            )
        }
        if (userItems.isEmpty()) {
            item(key = "user-empty") { EmptyUserSectionHint() }
        } else {
            items(userItems, key = { it.rowKey() }) { item ->
                CategoryRow(
                    item = item,
                    selected = item.id in selectedIds,
                    selectionMode = selectionMode,
                    onClick = { onCategoryClicked(item) },
                    onLongClick = { onCategoryLongClicked(item) },
                    onRenameClicked = { onRenameRequested(item) },
                    onDeleteClicked = { onDeleteRequested(item) },
                )
            }
        }
        stickyHeader(key = "header-builtin") {
            SectionHeader(
                title = stringResource(Res.string.recipe_categories_section_builtin),
                trailing = {},
            )
        }
        items(builtinItems, key = { it.rowKey() }) { item ->
            CategoryRow(
                item = item,
                selected = item.id in selectedIds,
                selectionMode = selectionMode,
                onClick = { onCategoryClicked(item) },
                onLongClick = { onCategoryLongClicked(item) },
                onRenameClicked = { onRenameRequested(item) },
                onDeleteClicked = { onDeleteRequested(item) },
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: @Composable () -> Unit) {
    Surface(
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth).fillMaxWidth(),
        color = ChefMateTheme.colorScheme.background,
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(start = ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = ChefMateTheme.typography.titleSmall,
                color = ChefMateTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
    }
}

@Composable
private fun EmptyUserSectionHint() {
    Box(
        modifier =
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                )
    ) {
        Text(
            text = stringResource(Res.string.recipe_categories_empty_user),
            style = MaterialTheme.typography.bodyMedium,
            color = ChefMateTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryRow(
    item: CategoryItem,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRenameClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    val rowAlpha = if (selectionMode && !item.isEditable) 0.4f else 1f
    Row(
        modifier =
            // widthIn must come before fillMaxWidth — fillMaxWidth pins min=max=parent first,
            // and a later widthIn can't shrink it.
            Modifier.widthIn(max = PlusHeaderContainerDefaults.MaxContentWidth)
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .combinedClickable(
                    enabled = item.isEditable || !selectionMode,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(horizontal = ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectionMode) {
            if (item.isEditable) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall),
                )
            } else {
                // Spacer so built-in rows line up visually with checkbox rows.
                Box(modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.displayName(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall),
            )
            Text(
                text = item.subtitleText(),
                style = MaterialTheme.typography.bodySmall,
                color = ChefMateTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!selectionMode && item.isEditable) {
            CategoryRowOverflowMenu(
                categoryName = item.name,
                onRenameClicked = onRenameClicked,
                onDeleteClicked = onDeleteClicked,
            )
        }
    }
    if (rowAlpha < 1f) {
        // No-op; alpha hint kept for future styling. The combinedClickable's `enabled = false`
        // already prevents interaction; rendering full opacity is acceptable.
    }
}

@Composable
private fun CategoryRowOverflowMenu(
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
                    PhraseModel(
                            Res.string.category_more_a11y,
                            "category" to FixedString(categoryName),
                        )
                        .localized(),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.category_rename)) },
                onClick = {
                    expanded = false
                    onRenameClicked()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.category_delete)) },
                onClick = {
                    expanded = false
                    onDeleteClicked()
                },
            )
        }
    }
}

@Composable
private fun CategoryItem.displayName(): String {
    val builtin = BuiltinCategory.fromId(builtinId)
    return if (builtin != null) stringResource(builtin.pickerLabelRes()) else name
}

@Composable
private fun CategoryItem.subtitleText(): String {
    val countText =
        when (recipeCount) {
            0 -> stringResource(Res.string.recipe_categories_recipe_count_zero)
            1 -> stringResource(Res.string.recipe_categories_recipe_count_one)
            else ->
                PhraseModel(
                        Res.string.recipe_categories_recipe_count_other,
                        "count" to FixedString(recipeCount.toString()),
                    )
                    .localized()
        }
    return if (isBuiltin) {
        "${stringResource(Res.string.recipe_categories_built_in_label)} • $countText"
    } else {
        countText
    }
}

private fun CategoryItem.rowKey(): String =
    if (id != 0L) "id-$id" else "builtin-${builtinId.orEmpty()}"
