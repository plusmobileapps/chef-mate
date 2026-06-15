package com.plusmobileapps.chefmate.recipe.core.impl.addmeal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_search_recipes
import chefmate.client.recipe.core.public.generated.resources.add_meal_plan_select_recipe
import com.plusmobileapps.chefmate.recipe.core.addmeal.RecipePickerBloc
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipePickerScreen(
    bloc: RecipePickerBloc,
    onCloseClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()

    PlusHeaderContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Modal(
                title = stringResource(Res.string.add_meal_plan_select_recipe).asTextData(),
                onCloseClick = onCloseClick,
            ),
        scrollEnabled = false,
    ) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = bloc::onSearchQueryChanged,
            placeholder = { Text(stringResource(Res.string.add_meal_plan_search_recipes)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(
                        horizontal = ChefMateTheme.dimens.paddingNormal,
                        vertical = ChefMateTheme.dimens.paddingSmall,
                    ),
        )

        if (state.isLoading) {
            PlusLoadingIndicator(
                modifier = Modifier.fillMaxWidth().padding(ChefMateTheme.dimens.paddingNormal)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().weight(1f)) {
                items(state.recipes, key = { it.id }) { recipe ->
                    RecipePickerItem(recipe = recipe, onClick = { bloc.onRecipeSelected(recipe) })
                }
            }
        }
    }
}

@Composable
private fun RecipePickerItem(
    recipe: RecipePickerBloc.RecipePickerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                ),
        horizontalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier = Modifier.size(48.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = recipe.title, style = MaterialTheme.typography.bodyLarge)
            if (recipe.bookNames.isNotEmpty()) {
                Text(
                    text = recipe.bookNames.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
