@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.grocery.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.grocery.data.GroceryItem

@Composable
fun GroceryDetailScreen(bloc: GroceryDetailBloc) {
    val state = bloc.models.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail") },
                navigationIcon = {
                    IconButton(bloc::onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = bloc::onSaveClicked,
            ) {
                Text("Save")
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(it)
        ) {
            when (val model = state.value) {
                is GroceryDetailBloc.Model.Loading -> CircularProgressIndicator()
                is GroceryDetailBloc.Model.Loaded -> GroceryDetailBody(model.item, bloc)
            }

        }
    }
}

@Composable
fun ColumnScope.GroceryDetailBody(
    item: GroceryItem,
    bloc: GroceryDetailBloc,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = item.name,
        onValueChange = bloc::onGroceryNameChanged,
    )
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { bloc.onGroceryCheckedChanged(!item.isChecked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isChecked,
            onCheckedChange = bloc::onGroceryCheckedChanged,
        )
        Text("Purchased")
    }
}