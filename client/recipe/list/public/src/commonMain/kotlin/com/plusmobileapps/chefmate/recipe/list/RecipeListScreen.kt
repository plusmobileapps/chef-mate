package com.plusmobileapps.chefmate.recipe.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.list.public.generated.resources.Res
import chefmate.client.recipe.list.public.generated.resources.recipe_list_add_recipe
import chefmate.client.recipe.list.public.generated.resources.recipe_list_apply
import chefmate.client.recipe.list.public.generated.resources.recipe_list_clear_filters
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_browse
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_create
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_description
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_by
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_empty_description
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_empty_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_favorites
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_quick_recipes
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_rated
import chefmate.client.recipe.list.public.generated.resources.recipe_list_item_calories
import chefmate.client.recipe.list.public.generated.resources.recipe_list_item_servings
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search_clear
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search_empty
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search_placeholder
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_a_to_z
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_and_filter
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_by
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_oldest_first
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_recently_added
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_top_rated
import chefmate.client.recipe.list.public.generated.resources.recipe_list_sort_z_to_a
import chefmate.client.recipe.list.public.generated.resources.recipe_list_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_view_grid
import chefmate.client.recipe.list.public.generated.resources.recipe_list_view_list
import chefmate.client.recipe.list.public.generated.resources.recipe_sync_not_synced
import chefmate.client.recipe.list.public.generated.resources.recipe_sync_synced
import chefmate.client.recipe.list.public.generated.resources.recipe_sync_syncing
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(bloc: RecipeListBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    var showSearchBar by remember { mutableStateOf(state.isSearchActive) }
    var showSortFilterSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(state.currentSort, state.activeFilters) {
        listState.animateScrollToItem(0)
        gridState.animateScrollToItem(0)
    }

    PlusNavContainer(
        modifier = modifier.fillMaxSize(),
        data =
            PlusHeaderData.Parent(
                title = Res.string.recipe_list_title.asTextData(),
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        IconButton(
                            onClick = {
                                showSearchBar = !showSearchBar
                                if (!showSearchBar) bloc.onSearchQueryChanged("")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(Res.string.recipe_list_search),
                            )
                        }
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
                        IconButton(onClick = { showSortFilterSheet = true }) {
                            val filterCount = state.activeFilters.size
                            if (filterCount > 0) {
                                BadgedBox(badge = { Badge { Text("$filterCount") } }) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription =
                                            stringResource(Res.string.recipe_list_filter),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription =
                                        stringResource(Res.string.recipe_list_filter),
                                )
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
            AnimatedVisibility(
                visible = showSearchBar,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChanged = bloc::onSearchQueryChanged,
                    onClear = {
                        bloc.onSearchQueryChanged("")
                        showSearchBar = false
                    },
                )
            }
            when {
                !state.isLoading && state.totalRecipeCount == 0 -> {
                    NoRecipesEmptyState(
                        onBrowseClicked = bloc::onBrowseRecipesClicked,
                        onCreateClicked = bloc::onAddRecipeClicked,
                        modifier = Modifier.weight(1f),
                    )
                }
                state.recipes.isEmpty() && state.isSearchActive -> {
                    SearchEmptyState(modifier = Modifier.weight(1f))
                }
                state.recipes.isEmpty() && state.activeFilters.isNotEmpty() -> {
                    FilterEmptyState(
                        activeFilters = state.activeFilters,
                        onClearFilters = bloc::onClearFilters,
                        modifier = Modifier.weight(1f),
                    )
                }
                state.isGridView -> {
                    RecipeGrid(
                        modifier = Modifier.weight(1f),
                        recipes = state.recipes,
                        onRecipeClicked = bloc::onRecipeClicked,
                        state = gridState,
                    )
                }
                else -> {
                    RecipeList(
                        modifier = Modifier.weight(1f),
                        recipes = state.recipes,
                        onRecipeClicked = bloc::onRecipeClicked,
                        state = listState,
                    )
                }
            }
        },
    )

    if (showSortFilterSheet) {
        SortFilterBottomSheet(
            currentSort = state.currentSort,
            activeFilters = state.activeFilters,
            onApply = { sort, filters ->
                bloc.onApplySortAndFilters(sort, filters)
                showSortFilterSheet = false
            },
            onDismiss = { showSortFilterSheet = false },
        )
    }
}

// region Sort & Filter Bottom Sheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SortFilterBottomSheet(
    currentSort: RecipeSortOption,
    activeFilters: Set<RecipeFilterOption>,
    onApply: (sort: RecipeSortOption, filters: Set<RecipeFilterOption>) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var selectedSort by remember { mutableStateOf(currentSort) }
    var selectedFilters by remember { mutableStateOf(activeFilters) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).navigationBarsPadding()) {
            Text(
                text = stringResource(Res.string.recipe_list_sort_and_filter),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.height(16.dp))

            // Sort by
            Text(
                text = stringResource(Res.string.recipe_list_sort_by),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecipeSortOption.entries.forEach { option ->
                    FilterChip(
                        selected = option == selectedSort,
                        onClick = { selectedSort = option },
                        label = { Text(stringResource(option.labelRes())) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Filter by
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.recipe_list_filter_by),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (selectedFilters.isNotEmpty()) {
                    TextButton(onClick = { selectedFilters = emptySet() }) {
                        Text(stringResource(Res.string.recipe_list_clear_filters))
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RecipeFilterOption.entries.forEach { filter ->
                    FilterChip(
                        selected = filter in selectedFilters,
                        onClick = {
                            selectedFilters =
                                if (filter in selectedFilters) selectedFilters - filter
                                else selectedFilters + filter
                        },
                        label = { Text(stringResource(filter.labelRes())) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { onApply(selectedSort, selectedFilters) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.recipe_list_apply))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// endregion

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

// region Search

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .focusRequester(focusRequester),
        placeholder = { Text(stringResource(Res.string.recipe_list_search_placeholder)) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onClear) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.recipe_list_search_clear),
                )
            }
        },
        singleLine = true,
    )
}

@Composable
private fun SearchEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.recipe_list_search_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

// endregion

// region Empty States

@Composable
private fun NoRecipesEmptyState(
    onBrowseClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.recipe_list_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.recipe_list_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onBrowseClicked, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(Res.string.recipe_list_empty_browse))
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onCreateClicked, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(stringResource(Res.string.recipe_list_empty_create))
        }
    }
}

@Composable
private fun FilterEmptyState(
    activeFilters: Set<RecipeFilterOption>,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterLabels =
        activeFilters.map { filter -> stringResource(filter.labelRes()) }.joinToString(", ")

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.recipe_list_filter_empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text =
                PhraseModel(
                        Res.string.recipe_list_filter_empty_description,
                        "filters" to FixedString(filterLabels),
                    )
                    .localized(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onClearFilters) {
            Text(stringResource(Res.string.recipe_list_clear_filters))
        }
    }
}

// endregion

// region Grid View

@Composable
private fun RecipeGrid(
    recipes: List<RecipeListItem>,
    onRecipeClicked: (RecipeListItem) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        state = state,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StarRating(rating = recipe.starRating, starSize = 14.dp)
                SyncStatusIcon(syncStatus = recipe.syncStatus)
            }
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
    state: LazyListState = rememberLazyListState(),
) {
    LazyColumn(state = state, modifier = modifier.fillMaxWidth()) {
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                SyncStatusIcon(syncStatus = recipe.syncStatus)
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
private fun SyncStatusIcon(syncStatus: SyncStatus, modifier: Modifier = Modifier) {
    val syncingDescription = stringResource(Res.string.recipe_sync_syncing)
    when (syncStatus) {
        SyncStatus.NOT_SYNCED ->
            Icon(
                imageVector = Icons.Outlined.CloudOff,
                contentDescription = stringResource(Res.string.recipe_sync_not_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(16.dp),
            )
        SyncStatus.SYNCING ->
            CircularProgressIndicator(
                modifier =
                    modifier.size(14.dp).semantics { contentDescription = syncingDescription },
                strokeWidth = 2.dp,
            )
        SyncStatus.SYNCED ->
            Icon(
                imageVector = Icons.Outlined.CloudDone,
                contentDescription = stringResource(Res.string.recipe_sync_synced),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.size(16.dp),
            )
    }
}

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
