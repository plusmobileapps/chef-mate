package com.plusmobileapps.chefmate.recipe.core.addgrocery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_add
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_no_ingredients
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_select_list
import com.plusmobileapps.chefmate.grocery.core.list.GroceryDisplayGroup
import com.plusmobileapps.chefmate.grocery.core.list.GroceryDisplayItem
import com.plusmobileapps.chefmate.grocery.core.list.GroceryGroupedList
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.lastItemFloatingActionButtonSpacer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeToGroceryListScreen(bloc: AddRecipeToGroceryListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Modal(
                title = stringResource(Res.string.recipe_add_to_grocery_list).asTextData(),
                onCloseClick = bloc::onBackClicked,
            ),
        scrollEnabled = false,
        floatingActionButton = {
            if (!state.isLoading && state.hasSelectedIngredients) {
                ExtendedFloatingActionButton(onClick = bloc::onSaveClicked) {
                    if (state.isAdding) {
                        PlusLoadingIndicator(
                            modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall)
                        )
                    }
                    Text(stringResource(Res.string.recipe_add_to_grocery_list_add))
                }
            }
        },
    ) {
        when {
            state.isLoading -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    PlusLoadingIndicator()
                }
            }
            state.isEmpty -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(Res.string.recipe_add_to_grocery_list_no_ingredients),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                GroceryListSelector(
                    groceryLists = state.groceryLists,
                    selectedList = state.selectedGroceryList,
                    onListSelected = bloc::onGroceryListSelected,
                )
                GroceryGroupedList(
                    groups =
                        state.groupedIngredients.map { group ->
                            GroceryDisplayGroup(
                                category = group.category,
                                items =
                                    group.items.map { item ->
                                        GroceryDisplayItem(
                                            key = item.id,
                                            displayName = item.displayName,
                                            quantity = item.quantity,
                                            isChecked = item.isSelected,
                                        )
                                    },
                            )
                        },
                    onItemClick = { key -> bloc.onIngredientToggled(key as Int) },
                    onCheckedChange = { key -> bloc.onIngredientToggled(key as Int) },
                    modifier = Modifier.weight(1f),
                    footer = { lastItemFloatingActionButtonSpacer() },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroceryListSelector(
    groceryLists: List<AddRecipeToGroceryListBloc.GroceryListItem>,
    selectedList: AddRecipeToGroceryListBloc.GroceryListItem?,
    onListSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (groceryLists.size <= 1) return
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        OutlinedTextField(
            value = selectedList?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.recipe_add_to_grocery_list_select_list)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            groceryLists.forEach { list ->
                DropdownMenuItem(
                    text = { Text(list.name) },
                    onClick = {
                        onListSelected(list.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
