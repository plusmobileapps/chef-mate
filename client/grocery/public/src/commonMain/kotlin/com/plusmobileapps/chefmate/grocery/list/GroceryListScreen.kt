package com.plusmobileapps.chefmate.grocery.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.grocery.data.GroceryItem
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroceryListScreen(bloc: GroceryListBloc) {
    val state by bloc.state.collectAsState()
    Scaffold(
        modifier = Modifier.fillMaxWidth(),
        topBar = {
            TopAppBar(title = { Text("Grocery List") })
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(state.items.size) { index ->
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
        }
    }
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
                Icon(Icons.Default.Add, contentDescription = "Add Item")
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
                Icon(Icons.Default.CheckBox, contentDescription = "Checked")
            } else {
                Icon(Icons.Default.CheckBoxOutlineBlank, contentDescription = "Not Checked")
            }
        }
        Text(
            text = item.name,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
        )

        IconButton(
            onClick = { onDeleteClick(item) },
        ) {
            Icon(Icons.Default.Delete, "Delete Item")
        }
    }
}
