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
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import chefmate.client.grocery.core.public.generated.resources.Res
import chefmate.client.grocery.core.public.generated.resources.grocery_add_item
import chefmate.client.grocery.core.public.generated.resources.grocery_checked
import chefmate.client.grocery.core.public.generated.resources.grocery_delete_item
import chefmate.client.grocery.core.public.generated.resources.grocery_list
import chefmate.client.grocery.core.public.generated.resources.grocery_not_checked
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
                trailingAccessory = PlusHeaderData.TrailingAccessory.Icon(
                    icon = Icons.Default.Sync,
                    contentDesc = Res.string.grocery_sync_all.asTextData(),
                    onClick = bloc::onSyncClicked,
                ),
            ),
        scrollEnabled = false,
        content = {
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
