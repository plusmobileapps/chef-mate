package com.plusmobileapps.chefmate.recipe.core.addgrocery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_add
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_no_ingredients
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.lastItemFloatingActionButtonSpacer
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeToGroceryListScreen(
    bloc: AddRecipeToGroceryListBloc,
    modifier: Modifier = Modifier,
) {
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
            if (!state.isLoading && state.ingredients.any { it.isSelected }) {
                ExtendedFloatingActionButton(
                    onClick = bloc::onSaveClicked,
                ) {
                    if (state.isAdding) {
                        PlusLoadingIndicator(
                            modifier = Modifier.padding(end = ChefMateTheme.dimens.paddingSmall),
                        )
                    }
                    Text(stringResource(Res.string.recipe_add_to_grocery_list_add))
                }
            }
        },
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    PlusLoadingIndicator()
                }
            }
            state.ingredients.isEmpty() -> {
                Box(
                    modifier = Modifier.weight(1f),
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

@Composable
private fun IngredientsList(
    ingredients: List<AddRecipeToGroceryListBloc.Model.ListItem>,
    onIngredientToggled: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(ingredients.size, key = { it }) { index ->
            val ingredient = ingredients[index]
            IngredientListItem(
                ingredient = ingredient,
                onToggle = { onIngredientToggled(ingredient.id) },
            )
        }

        lastItemFloatingActionButtonSpacer()
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
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                ),
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
