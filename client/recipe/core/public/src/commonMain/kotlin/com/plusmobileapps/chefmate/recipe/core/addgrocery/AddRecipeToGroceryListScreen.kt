package com.plusmobileapps.chefmate.recipe.core.addgrocery

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.plusmobileapps.chefmate.grocery.core.displayName
import com.plusmobileapps.chefmate.grocery.data.GroceryCategory
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.lastItemFloatingActionButtonSpacer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                GroupedIngredientsList(
                    groups = state.groupedIngredients,
                    onIngredientToggled = bloc::onIngredientToggled,
                    modifier = Modifier.weight(1f),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupedIngredientsList(
    groups: List<AddRecipeToGroceryListBloc.IngredientGroup>,
    onIngredientToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        groups.forEach { group ->
            stickyHeader(key = "header_${group.category.name}") {
                CategoryHeader(category = group.category)
            }
            items(group.items.size, key = { group.items[it].id }) { index ->
                val ingredient = group.items[index]
                IngredientListItem(
                    ingredient = ingredient,
                    onToggle = { onIngredientToggled(ingredient.id) },
                )
            }
        }

        lastItemFloatingActionButtonSpacer()
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
private fun IngredientListItem(
    ingredient: AddRecipeToGroceryListBloc.ListItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = ingredient.isSelected, onCheckedChange = { onToggle() })
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
