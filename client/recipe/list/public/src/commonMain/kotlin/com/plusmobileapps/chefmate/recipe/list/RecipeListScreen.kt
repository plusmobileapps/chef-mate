package com.plusmobileapps.chefmate.recipe.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.list.public.generated.resources.Res
import chefmate.client.recipe.list.public.generated.resources.recipe_list_add_recipe
import chefmate.client.recipe.list.public.generated.resources.recipe_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeListScreen(
    bloc: RecipeListBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        RecipeListHeader(
            onAddRecipeClicked = bloc::onAddRecipeClicked,
        )
        RecipeList(
            modifier = Modifier.weight(1f),
            recipes = state.recipes,
            onRecipeClicked = bloc::onRecipeClicked,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeListHeader(
    onAddRecipeClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(stringResource(Res.string.recipe_list_title)) },
        actions = {
            IconButton(onClick = onAddRecipeClicked) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.recipe_list_add_recipe),
                )
            }
        },
        windowInsets = WindowInsets(left = 0.dp),
        modifier = modifier,
    )
}

@Composable
private fun RecipeList(
    recipes: List<RecipeListItem>,
    onRecipeClicked: (RecipeListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
    ) {
        items(recipes.size, key = { recipes[it].id }) { index ->
            val recipe = recipes[index]
            RecipeListItemContent(
                recipe = recipe,
                onClick = { onRecipeClicked(recipe) },
            )
        }
    }
}

@Composable
private fun RecipeListItemContent(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Placeholder for recipe image
        Box(
            modifier =
                Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = recipe.title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            recipe.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
