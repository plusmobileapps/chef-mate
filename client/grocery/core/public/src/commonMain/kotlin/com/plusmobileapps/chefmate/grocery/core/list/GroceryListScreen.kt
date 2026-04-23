package com.plusmobileapps.chefmate.grocery.core.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_add_item
import chefmate.client.grocery.core.public.generated.resources.grocery_cancel
import chefmate.client.grocery.core.public.generated.resources.grocery_checked
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
import chefmate.client.grocery.core.public.generated.resources.grocery_filter
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_all
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_purchased
import chefmate.client.grocery.core.public.generated.resources.grocery_filter_unpurchased
import chefmate.client.grocery.core.public.generated.resources.grocery_list
import chefmate.client.grocery.core.public.generated.resources.grocery_not_checked
import chefmate.client.grocery.core.public.generated.resources.grocery_select_list
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_all
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_not_synced
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_synced
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_syncing
import com.plusmobileapps.chefmate.grocery.core.displayName
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.SyncStatus
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GroceryListScreen(bloc: GroceryListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    PlusNavContainer(
        modifier = modifier.fillMaxSize(),
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
                    FilterButton(filter = state.filter, onFilterChanged = bloc::onFilterChanged)
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
            LazyColumn(modifier = Modifier.weight(1f)) {
                state.groupedItems.forEach { group ->
                    stickyHeader(key = "header_${group.category.name}") {
                        CategoryHeader(category = group.category)
                    }
                    items(group.items.size, key = { group.items[it].id }) { index ->
                        val item = group.items[index]
                        GroceryListItem(
                            item = item,
                            onCheckedChange = bloc::onGroceryItemCheckedChange,
                            onDeleteClick = bloc::onGroceryItemDelete,
                            onGroceryClick = bloc::onGroceryItemClicked,
                        )
                    }
                }
            }
            GroceryListInput(
                name = bloc.newGroceryItemName,
                onNameChange = bloc::onNewGroceryItemNameChange,
                onAddClick = bloc::saveGroceryItem,
            )
        },
    )

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
}

@Composable
private fun CategoryHeader(category: GroceryCategory, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = modifier.fillMaxWidth()) {
        Text(
            text = category.displayName().localized(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FilterButton(
    filter: GroceryListBloc.GroceryFilter,
    onFilterChanged: (GroceryListBloc.GroceryFilter) -> Unit,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    IconButton(onClick = { filterExpanded = true }) {
        Icon(
            Icons.Default.FilterList,
            contentDescription = stringResource(Res.string.grocery_filter),
        )
    }
    DropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
        GroceryListBloc.GroceryFilter.entries.forEach { filterOption ->
            val label =
                when (filterOption) {
                    GroceryListBloc.GroceryFilter.ALL ->
                        stringResource(Res.string.grocery_filter_all)
                    GroceryListBloc.GroceryFilter.UNPURCHASED ->
                        stringResource(Res.string.grocery_filter_unpurchased)
                    GroceryListBloc.GroceryFilter.PURCHASED ->
                        stringResource(Res.string.grocery_filter_purchased)
                }
            DropdownMenuItem(
                text = {
                    Text(
                        text = label,
                        color =
                            if (filter == filterOption) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                },
                onClick = {
                    onFilterChanged(filterOption)
                    filterExpanded = false
                },
            )
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
    OutlinedTextField(
        value = state.value,
        onValueChange = onNameChange,
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = onAddClick, enabled = state.value.isNotBlank()) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(Res.string.grocery_add_item),
                )
            }
        },
    )
}

@Composable
private fun GroceryListItem(
    item: GroceryItem,
    onCheckedChange: (GroceryItem, Boolean) -> Unit,
    onDeleteClick: (GroceryItem) -> Unit,
    onGroceryClick: (GroceryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable { onGroceryClick(item) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onCheckedChange(item, !item.isChecked) }) {
            if (item.isChecked) {
                Icon(
                    Icons.Default.CheckBox,
                    contentDescription = stringResource(Res.string.grocery_checked),
                )
            } else {
                Icon(
                    Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = stringResource(Res.string.grocery_not_checked),
                )
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(text = item.displayName, style = MaterialTheme.typography.bodyLarge)
            val quantity = item.quantity
            if (quantity != null) {
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

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

        IconButton(onClick = { onDeleteClick(item) }) {
            Icon(Icons.Default.Delete, stringResource(Res.string.grocery_delete_item))
        }
    }
}
