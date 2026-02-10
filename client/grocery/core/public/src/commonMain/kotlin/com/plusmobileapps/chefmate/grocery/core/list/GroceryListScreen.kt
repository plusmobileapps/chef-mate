package com.plusmobileapps.chefmate.grocery.core.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import chefmate.client.grocery.core.public.generated.resources.grocery_checked
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_cancel
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_confirm
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_hint
import chefmate.client.grocery.core.public.generated.resources.grocery_create_list_title
import chefmate.client.grocery.core.public.generated.resources.grocery_create_new_list
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_item
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_list
import chefmate.client.grocery.core.public.generated.resources.grocery_list
import chefmate.client.grocery.core.public.generated.resources.grocery_not_checked
import chefmate.client.grocery.core.public.generated.resources.grocery_select_list
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_all
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_not_synced
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_synced
import chefmate.client.grocery.core.public.generated.resources.grocery_sync_syncing
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import com.plusmobileapps.chefmate.grocery.data.SyncStatus
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(
    bloc: GroceryListBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()
    PlusNavContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Parent(
                title = Res.string.grocery_list.asTextData(),
                trailingAccessory = if (state.isSyncing) {
                    PlusHeaderData.TrailingAccessory.Custom {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                } else {
                    PlusHeaderData.TrailingAccessory.Icon(
                        icon = Icons.Default.Sync,
                        contentDesc = Res.string.grocery_sync_all.asTextData(),
                        onClick = bloc::onSyncClicked,
                    )
                },
            ),
        scrollEnabled = false,
        content = {
            GroceryListSelector(
                state = state,
                onListSelected = bloc::onListSelected,
                onCreateListClicked = bloc::onCreateListClicked,
                onDeleteListClicked = bloc::onDeleteListClicked,
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(state.items.size, key = { state.items[it].id }) { index ->
                    val item = state.items[index]
                    GroceryListItem(
                        item = item,
                        onCheckedChange = bloc::onGroceryItemCheckedChange,
                        onDeleteClick = bloc::onGroceryItemDelete,
                        onGroceryClick = bloc::onGroceryItemClicked,
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

    if (state.showCreateListDialog) {
        CreateListDialog(
            onDismiss = bloc::onCreateListDismissed,
            onConfirm = bloc::onCreateListConfirmed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroceryListSelector(
    state: GroceryListBloc.Model,
    onListSelected: (com.plusmobileapps.chefmate.grocery.data.GroceryListModel) -> Unit,
    onCreateListClicked: () -> Unit,
    onDeleteListClicked: (com.plusmobileapps.chefmate.grocery.data.GroceryListModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.lists.size <= 1 && !state.lists.any { it.id != state.selectedList?.id }) {
        // Only show selector when there are multiple lists or to allow creating new ones
    }
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        OutlinedTextField(
            value = state.selectedList?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.grocery_select_list)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            state.lists.forEach { list ->
                DropdownMenuItem(
                    text = { Text(list.name) },
                    onClick = {
                        onListSelected(list)
                        expanded = false
                    },
                    trailingIcon = if (state.lists.size > 1) {
                        {
                            IconButton(
                                onClick = {
                                    expanded = false
                                    onDeleteListClicked(list)
                                },
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.grocery_delete_list),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        null
                    },
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.grocery_create_new_list)) },
                onClick = {
                    expanded = false
                    onCreateListClicked()
                },
                leadingIcon = {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                },
            )
        }
    }
}

@Composable
private fun CreateListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
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
            TextButton(
                onClick = { onConfirm(listName) },
                enabled = listName.isNotBlank(),
            ) {
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
            IconButton(
                onClick = onAddClick,
                enabled = state.value.isNotBlank(),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.grocery_add_item))
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
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    onGroceryClick(item)
                },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onCheckedChange(item, !item.isChecked) },
        ) {
            if (item.isChecked) {
                Icon(Icons.Default.CheckBox, contentDescription = stringResource(Res.string.grocery_checked))
            } else {
                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = stringResource(Res.string.grocery_not_checked))
            }
        }
        Text(
            text = item.name,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
        )

        val syncingDescription = stringResource(Res.string.grocery_sync_syncing)
        when (item.syncStatus) {
            SyncStatus.NOT_SYNCED -> Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = stringResource(Res.string.grocery_sync_not_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            SyncStatus.SYNCING -> CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp)
                    .semantics { contentDescription = syncingDescription },
                strokeWidth = 2.dp,
            )
            SyncStatus.SYNCED -> Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = stringResource(Res.string.grocery_sync_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        IconButton(
            onClick = { onDeleteClick(item) },
        ) {
            Icon(Icons.Default.Delete, stringResource(Res.string.grocery_delete_item))
        }
    }
}
