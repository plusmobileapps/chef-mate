package com.plusmobileapps.chefmate.recipe.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.list.public.generated.resources.Res
import chefmate.client.recipe.list.public.generated.resources.recipe_list_add_recipe
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_favorites
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_quick_recipes
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_rated
import chefmate.client.recipe.list.public.generated.resources.recipe_list_item_calories
import chefmate.client.recipe.list.public.generated.resources.recipe_list_item_servings
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_a_to_z
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_oldest_first
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_recently_added
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_top_rated
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_z_to_a
import chefmate.client.recipe.list.public.generated.resources.recipe_list_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_view_grid
import chefmate.client.recipe.list.public.generated.resources.recipe_list_view_list
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeListScreen(bloc: RecipeListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    PlusNavContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Parent(
                title = Res.string.recipe_list_title.asTextData(),
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        IconButton(onClick = bloc::onToggleViewMode) {
                            Icon(
                                imageVector =
                                    if (state.isGridView) Icons.AutoMirrored.Filled.ViewList
                                    else Icons.Default.GridView,
                                contentDescription =
                                    stringResource(
                                        if (state.isGridView) {
                                            Res.string.recipe_list_view_list
                                        } else {
                                            Res.string.recipe_list_view_grid
                                        }
                                    ),
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = stringResource(Res.string.recipe_list_sort),
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                            ) {
                                RecipeSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(stringResource(option.labelRes())) },
                                        onClick = {
                                            bloc.onSortOptionSelected(option)
                                            showSortMenu = false
                                        },
                                        leadingIcon =
                                            if (option == state.currentSort) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                    )
                                                }
                                            } else {
                                                null
                                            },
                                    )
                                }
                            }
                        }
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription =
                                        stringResource(Res.string.recipe_list_filter),
                                    tint =
                                        if (state.activeFilters.isNotEmpty()) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false },
                            ) {
                                RecipeFilterOption.entries.forEach { filter ->
                                    val isActive = filter in state.activeFilters
                                    DropdownMenuItem(
                                        text = { Text(stringResource(filter.labelRes())) },
                                        onClick = { bloc.onFilterToggled(filter) },
                                        leadingIcon =
                                            if (isActive) {
                                                {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                    )
                                                }
                                            } else {
                                                null
                                            },
                                    )
                                }
                            }
                        }
                        IconButton(onClick = bloc::onAddRecipeClicked) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription =
                                    stringResource(Res.string.recipe_list_add_recipe),
                            )
                        }
                    },
            ),
        scrollEnabled = false,
        content = {
            if (state.isGridView) {
                RecipeGrid(
                    modifier = Modifier.weight(1f),
                    recipes = state.recipes,
                    onRecipeClicked = bloc::onRecipeClicked,
                )
            } else {
                RecipeList(
                    modifier = Modifier.weight(1f),
                    recipes = state.recipes,
                    onRecipeClicked = bloc::onRecipeClicked,
                )
            }
        },
    )
}

private fun RecipeSortOption.labelRes(): StringResource =
    when (this) {
        RecipeSortOption.RECENTLY_ADDED -> Res.string.recipe_list_sort_recently_added
        RecipeSortOption.OLDEST_FIRST -> Res.string.recipe_list_sort_oldest_first
        RecipeSortOption.ALPHABETICAL_ASC -> Res.string.recipe_list_sort_a_to_z
        RecipeSortOption.ALPHABETICAL_DESC -> Res.string.recipe_list_sort_z_to_a
        RecipeSortOption.TOP_RATED -> Res.string.recipe_list_sort_top_rated
    }

private fun RecipeFilterOption.labelRes(): StringResource =
    when (this) {
        RecipeFilterOption.FAVORITES -> Res.string.recipe_list_filter_favorites
        RecipeFilterOption.RATED -> Res.string.recipe_list_filter_rated
        RecipeFilterOption.QUICK_RECIPES -> Res.string.recipe_list_filter_quick_recipes
    }

// region Grid View

@Composable
private fun RecipeGrid(
    recipes: List<RecipeListItem>,
    onRecipeClicked: (RecipeListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp),
    ) {
        items(recipes.size, key = { recipes[it].id }) { index ->
            val recipe = recipes[index]
            RecipeGridItem(recipe = recipe, onClick = { onRecipeClicked(recipe) })
        }
    }
}

@Composable
private fun RecipeGridItem(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(1.2f),
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.titleSmall,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            StarRating(rating = recipe.starRating, starSize = 14.dp)
        }
    }
}

// endregion

// region List View

@Composable
private fun RecipeList(
    recipes: List<RecipeListItem>,
    onRecipeClicked: (RecipeListItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(recipes.size, key = { recipes[it].id }) { index ->
            val recipe = recipes[index]
            RecipeListItemContent(recipe = recipe, onClick = { onRecipeClicked(recipe) })
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeListItemContent(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier = Modifier.size(80.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (recipe.isFavorite) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
            recipe.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            StarRating(rating = recipe.starRating, starSize = 16.dp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                recipe.formattedTotalTime?.let { formattedTime ->
                    RecipeInfoChip(
                        icon = Icons.Outlined.AccessTime,
                        label = formattedTime.localized(),
                    )
                }
                recipe.servings?.let { servings ->
                    RecipeInfoChip(
                        icon = Icons.Outlined.Restaurant,
                        label =
                            PhraseModel(
                                    Res.string.recipe_list_item_servings,
                                    "servings" to FixedString(servings.toString()),
                                )
                                .localized(),
                    )
                }
                recipe.calories?.let { calories ->
                    RecipeInfoChip(
                        icon = Icons.Outlined.LocalFireDepartment,
                        label =
                            PhraseModel(
                                    Res.string.recipe_list_item_calories,
                                    "calories" to FixedString(calories.toString()),
                                )
                                .localized(),
                    )
                }
            }
        }
    }
}

// endregion

// region Shared Components

@Composable
private fun StarRating(
    rating: Int?,
    starSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val displayRating = rating ?: 0
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            val isFilled = index < displayRating
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                modifier = Modifier.size(starSize),
                tint =
                    if (isFilled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
            )
        }
    }
}

@Composable
private fun RecipeInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// endregion
