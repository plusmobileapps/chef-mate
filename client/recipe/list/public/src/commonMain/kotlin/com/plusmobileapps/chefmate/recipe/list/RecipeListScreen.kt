package com.plusmobileapps.chefmate.recipe.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_a_to_z
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_oldest_first
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_recently_added
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_top_rated
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_z_to_a
import chefmate.client.recipe.list.public.generated.resources.recipe_list_title
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecipeListScreen(
    bloc: RecipeListBloc,
    modifier: Modifier = Modifier,
) {
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
                                    contentDescription = stringResource(Res.string.recipe_list_filter),
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
                                contentDescription = stringResource(Res.string.recipe_list_add_recipe),
                            )
                        }
                    },
            ),
        scrollEnabled = false,
        content = {
            RecipeList(
                modifier = Modifier.weight(1f),
                recipes = state.recipes,
                onRecipeClicked = bloc::onRecipeClicked,
            )
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
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier = Modifier.size(80.dp),
        )
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
            recipe.starRating?.let { rating ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(5) { index ->
                        val isFilled = index < rating
                        Icon(
                            imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint =
                                if (isFilled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                        )
                    }
                }
            }
        }
    }
}
