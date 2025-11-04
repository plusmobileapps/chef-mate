package com.plusmobileapps.chefmate.recipe.core.addgrocery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_add
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_back
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_no_ingredients
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeToGroceryListScreen(
    bloc: AddRecipeToGroceryListBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.recipe_add_to_grocery_list)) },
                navigationIcon = {
                    IconButton(onClick = bloc::onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(Res.string.recipe_add_to_grocery_list_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (!state.isLoading && state.ingredients.any { it.isSelected }) {
                ExtendedFloatingActionButton(
                    onClick = bloc::onSaveClicked,
                ) {
                    if (state.isAdding) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Text(stringResource(Res.string.recipe_add_to_grocery_list_add))
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.ingredients.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(Res.string.recipe_add_to_grocery_list_no_ingredients),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                else -> {
                    IngredientsList(
                        ingredients = state.ingredients,
                        onIngredientToggled = bloc::onIngredientToggled,
                    )
                }
            }
        }
    }
}

@Composable
private fun IngredientsList(
    ingredients: List<AddRecipeToGroceryListBloc.Model.ListItem>,
    onIngredientToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(ingredients.size, key = { ingredients[it].id }) { index ->
            val ingredient = ingredients[index]
            IngredientListItem(
                ingredient = ingredient,
                onToggle = { onIngredientToggled(ingredient.id) },
            )
        }
    }
}

@Composable
private fun IngredientListItem(
    ingredient: AddRecipeToGroceryListBloc.Model.ListItem,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = ingredient.isSelected,
            onCheckedChange = { onToggle() },
        )
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}
