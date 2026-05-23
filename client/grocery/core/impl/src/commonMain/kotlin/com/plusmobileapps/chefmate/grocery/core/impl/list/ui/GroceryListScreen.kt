package com.plusmobileapps.chefmate.grocery.core.impl.list.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_add_item
import chefmate.client.grocery.core.public.generated.resources.grocery_add_item_hint
import chefmate.client.grocery.core.public.generated.resources.grocery_apply
import chefmate.client.grocery.core.public.generated.resources.grocery_cancel
import chefmate.client.grocery.core.public.generated.resources.grocery_clear_filters
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_cancel
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_confirm
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_hint
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_title
import chefmate.client.grocery.core.public.generated.resources.grocery_create_new_list
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_all
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_item
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_items
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_items_message
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_items_title
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_list
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_purchased
import chefmate.client.grocery.core.public.generated.resources.grocery_detail
import chefmate.client.grocery.core.public.generated.resources.grocery_done
import chefmate.client.grocery.core.public.generated.resources.grocery_filter
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_all
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_by
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_by_recipe
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_no_recipe
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_purchased
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_unpurchased
import chefmate.client.grocery.core.public.generated.resources.grocery_list
import chefmate.client.grocery.core.public.generated.resources.grocery_list_empty_browse_recipes
import chefmate.client.grocery.core.public.generated.resources.grocery_list_empty_description
import chefmate.client.grocery.core.public.generated.resources.grocery_list_empty_title
import chefmate.client.grocery.core.public.generated.resources.grocery_select_list
import chefmate.client.grocery.core.public.generated.resources.grocery_sort_aisle
import chefmate.client.grocery.core.public.generated.resources.grocery_sort_alphabetical
import chefmate.client.grocery.core.public.generated.resources.grocery_sort_and_filter
import chefmate.client.grocery.core.public.generated.resources.grocery_sort_by
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_all
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_not_synced
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_synced
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_syncing
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.grocery.core.impl.detail.ui.GroceryDetailSheetContent
import com.plusmobileapps.chefmate.grocery.core.list.GroceryDisplayGroup
import com.plusmobileapps.chefmate.grocery.core.list.GroceryDisplayItem
import com.plusmobileapps.chefmate.grocery.core.list.GroceryGroupedList
import com.plusmobileapps.chefmate.grocery.core.list.GroceryListBloc
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.SyncStatus
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.PlusResponsiveModal
import com.plusmobileapps.chefmate.ui.isIosPlatform
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

object GroceryListTestTags {
    const val SCREEN = "grocery_list_screen"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(bloc: GroceryListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    var showSortFilterSheet by remember { mutableStateOf(false) }
    val hasActiveFilter = state.filter != GroceryListBloc.GroceryFilter.ALL
    val hasNonDefaultSort = state.sort != GroceryListBloc.GrocerySort.AISLE
    val hasRecipeFilter = state.recipeFilter != null
    val activeCount =
        (if (hasActiveFilter) 1 else 0) +
            (if (hasNonDefaultSort) 1 else 0) +
            (if (hasRecipeFilter) 1 else 0)
    val focusManager = LocalFocusManager.current

    PlusNavContainer(
        modifier = modifier.testTag(GroceryListTestTags.SCREEN).fillMaxSize(),
        data = PlusHeaderData.None,
        scrollEnabled = false,
        content = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = {
                    Row(
                        modifier = Modifier.clickable(onClick = bloc::onListSelectorClicked),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                state.selectedList?.name ?: stringResource(Res.string.grocery_list)
                        )
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.showListSelector)
                    }
                },
                actions = {
                    IconButton(onClick = { showSortFilterSheet = true }) {
                        if (activeCount > 0) {
                            BadgedBox(badge = { Badge { Text("$activeCount") } }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = stringResource(Res.string.grocery_filter),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = stringResource(Res.string.grocery_filter),
                            )
                        }
                    }
                    IconButton(onClick = bloc::onDeleteClicked) {
                        Icon(
                            Icons.Default.DeleteSweep,
                            contentDescription = stringResource(Res.string.grocery_delete_items),
                        )
                    }
                    if (state.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 4.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        IconButton(onClick = bloc::onSyncClicked) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = stringResource(Res.string.grocery_sync_all),
                            )
                        }
                    }
                },
            )
            val itemLookup =
                remember(state.groupedItems) {
                    state.groupedItems.flatMap { it.items }.associateBy { it.id }
                }
            PullToRefreshBox(
                isRefreshing = state.isSyncing,
                onRefresh = bloc::onSyncClicked,
                modifier = Modifier.weight(1f),
            ) {
                val showEmptyState =
                    state.groupedItems.isEmpty() &&
                        state.filter == GroceryListBloc.GroceryFilter.ALL &&
                        state.recipeFilter == null &&
                        !state.isSyncing
                if (showEmptyState) {
                    EmptyGroceryListState(
                        onBrowseRecipesClicked = bloc::onBrowseRecipesClicked,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    GroceryGroupedList(
                        groups =
                            state.groupedItems.map { group ->
                                GroceryDisplayGroup(
                                    category = group.category,
                                    items =
                                        group.items.map { item ->
                                            GroceryDisplayItem(
                                                key = item.id,
                                                displayName = item.displayName,
                                                quantity = item.quantity,
                                                isChecked = item.isChecked,
                                                recipeName = item.recipeName,
                                            )
                                        },
                                )
                            },
                        onItemClick = { key ->
                            itemLookup[key as Long]?.let { bloc.onGroceryItemClicked(it) }
                        },
                        onCheckedChange = { key ->
                            itemLookup[key as Long]?.let {
                                bloc.onGroceryItemCheckedChange(it, !it.isChecked)
                            }
                        },
                        modifier =
                            Modifier.fillMaxSize().pointerInput(Unit) {
                                detectTapGestures(onTap = { focusManager.clearFocus() })
                            },
                        showHeaders = state.sort == GroceryListBloc.GrocerySort.AISLE,
                        trailingContent = { displayItem ->
                            val item = itemLookup[displayItem.key as Long]
                            if (item != null) {
                                GroceryItemTrailingContent(
                                    item = item,
                                    onDeleteClick = { bloc.onGroceryItemDelete(item) },
                                )
                            }
                        },
                    )
                }
            }
            GroceryListInput(
                name = bloc.newGroceryItemName,
                onNameChange = bloc::onNewGroceryItemNameChange,
                onAddClick = bloc::saveGroceryItem,
            )
        },
    )

    if (showSortFilterSheet) {
        GrocerySortFilterBottomSheet(
            currentSort = state.sort,
            currentFilter = state.filter,
            currentRecipeFilter = state.recipeFilter,
            availableRecipes = state.availableRecipes,
            hasNoRecipeItems = state.hasNoRecipeItems,
            onApply = { sort, filter, recipeFilter ->
                bloc.onApplySortAndFilter(sort, filter, recipeFilter)
                showSortFilterSheet = false
            },
            onDismiss = { showSortFilterSheet = false },
        )
    }

    if (state.showListSelector) {
        GroceryListSelectorSheet(
            state = state,
            onDismiss = bloc::onListSelectorDismissed,
            onListSelected = bloc::onListSelected,
            onCreateListClicked = bloc::onCreateListClicked,
            onDeleteListClicked = bloc::onDeleteListClicked,
        )
    }

    if (state.showCreateListDialog) {
        CreateListDialog(
            onDismiss = bloc::onCreateListDismissed,
            onConfirm = bloc::onCreateListConfirmed,
        )
    }

    if (state.showDeleteDialog) {
        DeleteItemsDialog(
            onDismiss = bloc::onDeleteDismissed,
            onDeletePurchased = bloc::onDeletePurchasedConfirmed,
            onDeleteAll = bloc::onDeleteAllConfirmed,
        )
    }

    GroceryDetailSheet(bloc = bloc)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroceryDetailSheet(bloc: GroceryListBloc) {
    val slot = bloc.childSlot.subscribeAsState()
    val child = slot.value.child?.instance
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sheetChild by remember { mutableStateOf(child) }
    if (child != null) {
        sheetChild = child
    }

    LaunchedEffect(child) {
        if (child == null && sheetChild != null) {
            sheetState.hide()
            sheetChild = null
        }
    }

    val active = sheetChild
    if (active != null) {
        PlusResponsiveModal(
            onDismissRequest = bloc::onDismissSheet,
            sheetState = sheetState,
            title = Res.string.grocery_detail.asTextData(),
            onCloseClick = bloc::onDismissSheet,
        ) {
            when (active) {
                is GroceryListBloc.Sheet.GroceryDetail ->
                    GroceryDetailSheetContent(bloc = active.bloc)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GrocerySortFilterBottomSheet(
    currentSort: GroceryListBloc.GrocerySort,
    currentFilter: GroceryListBloc.GroceryFilter,
    currentRecipeFilter: String?,
    availableRecipes: List<String>,
    hasNoRecipeItems: Boolean,
    onApply:
        (
            sort: GroceryListBloc.GrocerySort,
            filter: GroceryListBloc.GroceryFilter,
            recipeFilter: String?,
        ) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSort by remember { mutableStateOf(currentSort) }
    var selectedFilter by remember { mutableStateOf(currentFilter) }
    var selectedRecipeFilter by remember { mutableStateOf(currentRecipeFilter) }

    val showRecipeSection = availableRecipes.isNotEmpty() || hasNoRecipeItems
    val hasActiveFilters =
        selectedSort != GroceryListBloc.GrocerySort.AISLE ||
            selectedFilter != GroceryListBloc.GroceryFilter.ALL ||
            selectedRecipeFilter != null

    val dimens = ChefMateTheme.dimens
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(horizontal = dimens.paddingNormal).navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.grocery_sort_and_filter),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (hasActiveFilters) {
                    TextButton(
                        onClick = {
                            selectedSort = GroceryListBloc.GrocerySort.AISLE
                            selectedFilter = GroceryListBloc.GroceryFilter.ALL
                            selectedRecipeFilter = null
                        }
                    ) {
                        Text(stringResource(Res.string.grocery_clear_filters))
                    }
                }
            }
            Spacer(Modifier.height(dimens.paddingNormal))

            Text(
                text = stringResource(Res.string.grocery_sort_by),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(dimens.paddingSmall))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall)) {
                GroceryListBloc.GrocerySort.entries.forEach { option ->
                    FilterChip(
                        selected = option == selectedSort,
                        onClick = { selectedSort = option },
                        label = {
                            Text(
                                when (option) {
                                    GroceryListBloc.GrocerySort.AISLE ->
                                        stringResource(Res.string.grocery_sort_aisle)
                                    GroceryListBloc.GrocerySort.ALPHABETICAL ->
                                        stringResource(Res.string.grocery_sort_alphabetical)
                                }
                            )
                        },
                    )
                }
            }
            Spacer(Modifier.height(dimens.paddingNormal))

            Text(
                text = stringResource(Res.string.grocery_filter_by),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(dimens.paddingSmall))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall)) {
                GroceryListBloc.GroceryFilter.entries.forEach { filterOption ->
                    FilterChip(
                        selected = filterOption == selectedFilter,
                        onClick = { selectedFilter = filterOption },
                        label = {
                            Text(
                                when (filterOption) {
                                    GroceryListBloc.GroceryFilter.ALL ->
                                        stringResource(Res.string.grocery_filter_all)
                                    GroceryListBloc.GroceryFilter.UNPURCHASED ->
                                        stringResource(Res.string.grocery_filter_unpurchased)
                                    GroceryListBloc.GroceryFilter.PURCHASED ->
                                        stringResource(Res.string.grocery_filter_purchased)
                                }
                            )
                        },
                    )
                }
            }

            if (showRecipeSection) {
                Spacer(Modifier.height(dimens.paddingNormal))
                Text(
                    text = stringResource(Res.string.grocery_filter_by_recipe),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(dimens.paddingSmall))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(dimens.paddingSmall)) {
                    availableRecipes.forEach { recipe ->
                        FilterChip(
                            selected = selectedRecipeFilter == recipe,
                            onClick = {
                                selectedRecipeFilter =
                                    if (selectedRecipeFilter == recipe) null else recipe
                            },
                            label = { Text(recipe) },
                        )
                    }
                    if (hasNoRecipeItems) {
                        FilterChip(
                            selected = selectedRecipeFilter == GroceryListBloc.NO_RECIPE_FILTER,
                            onClick = {
                                selectedRecipeFilter =
                                    if (selectedRecipeFilter == GroceryListBloc.NO_RECIPE_FILTER) {
                                        null
                                    } else {
                                        GroceryListBloc.NO_RECIPE_FILTER
                                    }
                            },
                            label = { Text(stringResource(Res.string.grocery_filter_no_recipe)) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(dimens.paddingNormal))
            Button(
                onClick = { onApply(selectedSort, selectedFilter, selectedRecipeFilter) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.grocery_apply))
            }
            Spacer(Modifier.height(dimens.paddingNormal))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroceryListSelectorSheet(
    state: GroceryListBloc.Model,
    onDismiss: () -> Unit,
    onListSelected: (com.plusmobileapps.chefmate.grocery.data.GroceryListModel) -> Unit,
    onCreateListClicked: () -> Unit,
    onDeleteListClicked: (com.plusmobileapps.chefmate.grocery.data.GroceryListModel) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Text(
            text = stringResource(Res.string.grocery_select_list),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        state.lists.forEach { list ->
            ListItem(
                headlineContent = {
                    Text(
                        text = list.name,
                        color =
                            if (list.id == state.selectedList?.id) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                },
                modifier = Modifier.clickable { onListSelected(list) },
                trailingContent =
                    if (state.lists.size > 1) {
                        {
                            IconButton(onClick = { onDeleteListClicked(list) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription =
                                        stringResource(Res.string.grocery_delete_list),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        null
                    },
            )
        }
        HorizontalDivider()
        ListItem(
            headlineContent = { Text(stringResource(Res.string.grocery_create_new_list)) },
            modifier =
                Modifier.clickable {
                    onDismiss()
                    onCreateListClicked()
                },
            leadingContent = {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            },
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CreateListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var listName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.grocery_create_list_title)) },
        text = {
            OutlinedTextField(
                value = listName,
                onValueChange = { listName = it },
                label = { Text(stringResource(Res.string.grocery_create_list_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(listName) }, enabled = listName.isNotBlank()) {
                Text(stringResource(Res.string.grocery_create_list_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.grocery_create_list_cancel))
            }
        },
    )
}

@Composable
private fun DeleteItemsDialog(
    onDismiss: () -> Unit,
    onDeletePurchased: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.grocery_delete_items_title)) },
        text = { Text(stringResource(Res.string.grocery_delete_items_message)) },
        confirmButton = {
            TextButton(onClick = onDeleteAll) {
                Text(stringResource(Res.string.grocery_delete_all))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text(stringResource(Res.string.grocery_cancel)) }
                TextButton(onClick = onDeletePurchased) {
                    Text(stringResource(Res.string.grocery_delete_purchased))
                }
            }
        },
    )
}

@Composable
private fun GroceryListInput(
    name: StateFlow<String>,
    onNameChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = name.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isIos = isIosPlatform()
    var isFocused by remember { mutableStateOf(false) }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = state.value,
            onValueChange = onNameChange,
            modifier = Modifier.weight(1f).onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            placeholder = { Text(stringResource(Res.string.grocery_add_item_hint)) },
            keyboardOptions =
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
            keyboardActions =
                KeyboardActions(
                    onDone = {
                        if (state.value.isNotBlank()) onAddClick()
                        keyboardController?.hide()
                    }
                ),
            trailingIcon = {
                IconButton(onClick = onAddClick, enabled = state.value.isNotBlank()) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.grocery_add_item),
                    )
                }
            },
        )
        AnimatedVisibility(
            visible = isIos && isFocused,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally(),
        ) {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
            ) {
                Text(stringResource(Res.string.grocery_done))
            }
        }
    }
}

@Composable
private fun GroceryItemTrailingContent(item: GroceryItem, onDeleteClick: () -> Unit) {
    val syncingDescription = stringResource(Res.string.grocery_sync_syncing)
    when (item.syncStatus) {
        SyncStatus.NOT_SYNCED ->
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = stringResource(Res.string.grocery_sync_not_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        SyncStatus.SYNCING ->
            CircularProgressIndicator(
                modifier =
                    Modifier.size(16.dp).semantics { contentDescription = syncingDescription },
                strokeWidth = 2.dp,
            )
        SyncStatus.SYNCED ->
            Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = stringResource(Res.string.grocery_sync_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
    }

    IconButton(onClick = onDeleteClick) {
        Icon(Icons.Default.Delete, stringResource(Res.string.grocery_delete_item))
    }
}

@Composable
private fun EmptyGroceryListState(
    onBrowseRecipesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.grocery_list_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.grocery_list_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBrowseRecipesClicked, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(Res.string.grocery_list_empty_browse_recipes))
        }
    }
}
