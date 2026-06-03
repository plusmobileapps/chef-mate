package com.plusmobileapps.chefmate.recipe.list.impl.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.list.public.generated.resources.Res
import chefmate.client.recipe.list.public.generated.resources.recipe_list_add_recipe
import chefmate.client.recipe.list.public.generated.resources.recipe_list_apply
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_ai
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_appetizer
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_breakfast
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_dessert
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_dinner
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_drink
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_lunch
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_other
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_side
import chefmate.client.recipe.list.public.generated.resources.recipe_list_category_snack
import chefmate.client.recipe.list.public.generated.resources.recipe_list_clear_filters
import chefmate.client.recipe.list.public.generated.resources.recipe_list_continue_cooking
import chefmate.client.recipe.list.public.generated.resources.recipe_list_create_recipe
import chefmate.client.recipe.list.public.generated.resources.recipe_list_done_cooking
import chefmate.client.recipe.list.public.generated.resources.recipe_list_done_cooking_cancel
import chefmate.client.recipe.list.public.generated.resources.recipe_list_done_cooking_confirm
import chefmate.client.recipe.list.public.generated.resources.recipe_list_done_cooking_message
import chefmate.client.recipe.list.public.generated.resources.recipe_list_done_cooking_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_browse
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_create
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_description
import chefmate.client.recipe.list.public.generated.resources.recipe_list_empty_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_by
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_by_category
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_empty_description
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_empty_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_favorites
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_quick_recipes
import chefmate.client.recipe.list.public.generated.resources.recipe_list_filter_rated
import chefmate.client.recipe.list.public.generated.resources.recipe_list_item_calories
import chefmate.client.recipe.list.public.generated.resources.recipe_list_item_servings
import chefmate.client.recipe.list.public.generated.resources.recipe_list_book_create
import chefmate.client.recipe.list.public.generated.resources.recipe_list_book_edit
import chefmate.client.recipe.list.public.generated.resources.recipe_list_book_picker_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_book_selector
import chefmate.client.recipe.list.public.generated.resources.recipe_list_menu_collaborate
import chefmate.client.recipe.list.public.generated.resources.recipe_list_menu_export_all
import chefmate.client.recipe.list.public.generated.resources.recipe_list_menu_select
import chefmate.client.recipe.list.public.generated.resources.recipe_list_more_actions
import chefmate.client.recipe.list.public.generated.resources.recipe_list_scan_failed_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_scan_from_photo
import chefmate.client.recipe.list.public.generated.resources.recipe_list_scanning_message
import chefmate.client.recipe.list.public.generated.resources.recipe_list_scanning_title
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search_clear
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search_empty
import chefmate.client.recipe.list.public.generated.resources.recipe_list_search_placeholder
import chefmate.client.recipe.list.public.generated.resources.recipe_list_selection_count
import chefmate.client.recipe.list.public.generated.resources.recipe_list_selection_deselect_all
import chefmate.client.recipe.list.public.generated.resources.recipe_list_selection_exit
import chefmate.client.recipe.list.public.generated.resources.recipe_list_selection_export
import chefmate.client.recipe.list.public.generated.resources.recipe_list_selection_select_all
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
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.SyncStatus
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListItem
import com.plusmobileapps.chefmate.recipe.list.RecipeListTestTags
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusDialog
import com.plusmobileapps.chefmate.ui.components.PlusDialogScaffold
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusNavContainer
import com.plusmobileapps.chefmate.ui.components.PlusResponsiveContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.components.WindowSizeClass
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberImagePickerLauncher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
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

    LaunchedEffect(Unit) {
        bloc.state
            .map { Triple(it.currentSort, it.activeFilters, it.activeCategories) }
            .distinctUntilChanged()
            .drop(1)
            .collect {
                listState.animateScrollToItem(0)
                gridState.animateScrollToItem(0)
            }
    }

    PlusResponsiveContainer { windowSizeClass ->
    val headerData =
        if (state.isSelectionMode) {
            PlusHeaderData.Parent(
                title =
                    PhraseModel(
                        Res.string.recipe_list_selection_count,
                        "count" to FixedString(state.selectedRecipeIds.size.toString()),
                    ),
                trailingAccessory =
                    PlusHeaderData.TrailingAccessory.Custom {
                        val allSelected =
                            state.recipes.isNotEmpty() &&
                                state.recipes.all { it.id in state.selectedRecipeIds }
                        IconButton(onClick = bloc::onToggleSelectAllVisible) {
                            Icon(
                                imageVector =
                                    if (allSelected) Icons.Default.CheckCircle
                                    else Icons.Outlined.Circle,
                                contentDescription =
                                    stringResource(
                                        if (allSelected) {
                                            Res.string.recipe_list_selection_deselect_all
                                        } else {
                                            Res.string.recipe_list_selection_select_all
                                        }
                                    ),
                            )
                        }
                        IconButton(
                            onClick = bloc::onExportClicked,
                            enabled = state.selectedRecipeIds.isNotEmpty(),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription =
                                    stringResource(Res.string.recipe_list_selection_export),
                            )
                        }
                        IconButton(onClick = bloc::onExitSelectionMode) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription =
                                    stringResource(Res.string.recipe_list_selection_exit),
                            )
                        }
                    },
            )
        } else {
            PlusHeaderData.Parent(
                // The book selector stands in for the title when a book is active, so the static
                // "Recipes" title is suppressed to avoid a cramped double-title in the app bar.
                title =
                    if (state.activeBook != null) FixedString("")
                    else Res.string.recipe_list_title.asTextData(),
                leading =
                    state.activeBook?.let { activeBook ->
                        {
                            BookSelector(
                                activeBookName = activeBook.name,
                                isPickerOpen = state.isBookPickerOpen,
                                onClick = bloc::onBookSelectorClicked,
                            ) {
                                // The dropdown is anchored to the selector on tablet/desktop widths.
                                if (windowSizeClass != WindowSizeClass.COMPACT) {
                                    BookPickerDropdown(
                                        expanded = state.isBookPickerOpen,
                                        books = state.recipeBooks,
                                        activeBookId = state.activeBook?.id,
                                        onDismiss = bloc::onBookPickerDismissed,
                                        onBookSelected = bloc::onBookSelected,
                                        onEditBook = bloc::onEditBookClicked,
                                        onCreateBook = bloc::onCreateBookClicked,
                                    )
                                }
                            }
                        }
                    },
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
                            val filterCount = state.totalActiveFilterCount
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
                        AddRecipeMenu(
                            scanEnabled = state.isScanFromPhotoEnabled,
                            onCreateClicked = bloc::onAddRecipeClicked,
                            onScanPicked = bloc::onScanRecipePhotoPicked,
                        )
                        OverflowMenu(
                            onSelectClicked = bloc::onEnterSelectionMode,
                            onExportAllClicked = bloc::onExportClicked,
                            onCollaborateClicked = bloc::onCollaborateClicked,
                        )
                    },
            )
        }

    Box(modifier = modifier.fillMaxSize().testTag(RecipeListTestTags.SCREEN)) {
        PlusNavContainer(
            modifier = modifier.fillMaxSize(),
            data = headerData,
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
                PullToRefreshBox(
                    isRefreshing = state.isSyncing,
                    onRefresh = bloc::onSyncClicked,
                    modifier = Modifier.weight(1f),
                ) {
                    when {
                        !state.isLoading && state.totalRecipeCount == 0 -> {
                            NoRecipesEmptyState(
                                onBrowseClicked = bloc::onBrowseRecipesClicked,
                                onCreateClicked = bloc::onAddRecipeClicked,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        state.recipes.isEmpty() && state.isSearchActive -> {
                            SearchEmptyState(modifier = Modifier.fillMaxSize())
                        }
                        state.recipes.isEmpty() && state.totalActiveFilterCount > 0 -> {
                            FilterEmptyState(
                                activeFilters = state.activeFilters,
                                activeCategories = state.activeCategories,
                                onClearFilters = bloc::onClearFilters,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        state.isGridView -> {
                            RecipeGrid(
                                modifier = Modifier.fillMaxSize(),
                                recipes = state.recipes,
                                onRecipeClicked = bloc::onRecipeClicked,
                                state = gridState,
                                bottomContentPadding =
                                    if (state.cookingRecipeCount > 0) FabStackReserve else 0.dp,
                                isSelectionMode = state.isSelectionMode,
                                selectedRecipeIds = state.selectedRecipeIds,
                            )
                        }
                        else -> {
                            RecipeList(
                                modifier = Modifier.fillMaxSize(),
                                recipes = state.recipes,
                                onRecipeClicked = bloc::onRecipeClicked,
                                state = listState,
                                bottomContentPadding =
                                    if (state.cookingRecipeCount > 0) FabStackReserve else 0.dp,
                                isSelectionMode = state.isSelectionMode,
                                selectedRecipeIds = state.selectedRecipeIds,
                            )
                        }
                    }
                }
            },
        )

        if (showSortFilterSheet) {
            SortFilterBottomSheet(
                currentSort = state.currentSort,
                activeFilters = state.activeFilters,
                activeCategories = state.activeCategories,
                activeUserCategoryIds = state.activeUserCategoryIds,
                availableUserCategories = state.availableUserCategories,
                onApply = { sort, filters, categories, userCategoryIds ->
                    bloc.onApplySortAndFilters(sort, filters, categories, userCategoryIds)
                    showSortFilterSheet = false
                },
                onDismiss = { showSortFilterSheet = false },
            )
        }

        if (state.cookingRecipeCount > 0) {
            CookingSessionFabStack(
                onContinueClicked = bloc::onContinueCookingClicked,
                onDoneCookingClicked = bloc::onDoneCookingClicked,
                modifier = Modifier.align(Alignment.BottomEnd),
            )
        }

        if (state.showDoneCookingDialog) {
            DoneCookingDialog(
                onConfirm = bloc::onDoneCookingConfirmed,
                onDismiss = bloc::onDoneCookingDismissed,
            )
        }

        if (state.isScanning) {
            ScanningDialog()
        }

        state.scanError?.let { error ->
            PlusDialog(
                title = ResourceString(Res.string.recipe_list_scan_failed_title),
                message = error,
                onConfirmClick = bloc::onScanErrorDismissed,
                onDismissRequest = bloc::onScanErrorDismissed,
            )
        }

        // On phone widths the book picker is a bottom sheet rather than an anchored dropdown.
        if (state.isBookPickerOpen && windowSizeClass == WindowSizeClass.COMPACT) {
            BookPickerSheet(
                books = state.recipeBooks,
                activeBookId = state.activeBook?.id,
                onDismiss = bloc::onBookPickerDismissed,
                onBookSelected = bloc::onBookSelected,
                onEditBook = bloc::onEditBookClicked,
                onCreateBook = bloc::onCreateBookClicked,
            )
        }
    }
    }
}

/**
 * The "+" action. When [scanEnabled] it opens a chooser — create a recipe by hand, or scan one from
 * a photo via Gemini vision (the image picker launches directly from the scan item). When disabled
 * it opens the blank editor directly with no menu, preserving the original add-recipe behaviour.
 */
@Composable
private fun AddRecipeMenu(
    scanEnabled: Boolean,
    onCreateClicked: () -> Unit,
    onScanPicked: (ByteArray, String) -> Unit,
) {
    if (!scanEnabled) {
        IconButton(
            onClick = onCreateClicked,
            modifier = Modifier.testTag(RecipeListTestTags.ADD_RECIPE_BUTTON),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.recipe_list_add_recipe),
            )
        }
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val scanPicker = rememberImagePickerLauncher { picked ->
        picked?.let { onScanPicked(it.bytes, it.fileExtension) }
    }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag(RecipeListTestTags.ADD_RECIPE_BUTTON),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.recipe_list_add_recipe),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.recipe_list_create_recipe)) },
                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                modifier = Modifier.testTag(RecipeListTestTags.ADD_MENU_CREATE),
                onClick = {
                    expanded = false
                    onCreateClicked()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.recipe_list_scan_from_photo)) },
                leadingIcon = { Icon(Icons.Default.AddPhotoAlternate, contentDescription = null) },
                modifier = Modifier.testTag(RecipeListTestTags.ADD_MENU_SCAN),
                onClick = {
                    expanded = false
                    scanPicker()
                },
            )
        }
    }
}

/** Non-cancellable progress dialog shown while a picked photo is being scanned into a recipe. */
@Composable
private fun ScanningDialog() {
    PlusDialogScaffold(
        onDismissRequest = {},
        header = { Text(stringResource(Res.string.recipe_list_scanning_title)) },
        content = {
            Row(
                modifier = Modifier.testTag(RecipeListTestTags.SCANNING_DIALOG),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                Text(stringResource(Res.string.recipe_list_scanning_message))
            }
        },
    )
}

@Composable
private fun CookingSessionFabStack(
    onContinueClicked: () -> Unit,
    onDoneCookingClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExtendedFloatingActionButton(
            onClick = onDoneCookingClicked,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            elevation = FloatingActionButtonDefaults.loweredElevation(),
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(Res.string.recipe_list_done_cooking),
                style = MaterialTheme.typography.labelLarge,
            )
        }
        ExtendedFloatingActionButton(
            onClick = onContinueClicked,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(imageVector = Icons.Default.SoupKitchen, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(Res.string.recipe_list_continue_cooking))
        }
    }
}

@Composable
private fun DoneCookingDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.recipe_list_done_cooking_title)) },
        text = { Text(stringResource(Res.string.recipe_list_done_cooking_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.recipe_list_done_cooking_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.recipe_list_done_cooking_cancel))
            }
        },
    )
}

@Composable
private fun OverflowMenu(
    onSelectClicked: () -> Unit,
    onExportAllClicked: () -> Unit,
    onCollaborateClicked: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.recipe_list_more_actions),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.recipe_list_menu_select)) },
                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                onClick = {
                    expanded = false
                    onSelectClicked()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.recipe_list_menu_export_all)) },
                leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) },
                onClick = {
                    expanded = false
                    onExportAllClicked()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.recipe_list_menu_collaborate)) },
                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                onClick = {
                    expanded = false
                    onCollaborateClicked()
                },
            )
        }
    }
}

@Composable
private fun BookSelector(
    activeBookName: String?,
    isPickerOpen: Boolean,
    onClick: () -> Unit,
    dropdown: @Composable () -> Unit,
) {
    Box {
        Row(
            modifier =
                Modifier.clickable(onClick = onClick)
                    .padding(
                        horizontal = ChefMateTheme.dimens.paddingSmall,
                        vertical = ChefMateTheme.dimens.paddingExtraSmall,
                    )
                    .testTag(RecipeListTestTags.BOOK_SELECTOR)
                    .semantics { contentDescription = activeBookName ?: "" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = activeBookName.orEmpty(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 160.dp),
            )
            Icon(
                imageVector =
                    if (isPickerOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = stringResource(Res.string.recipe_list_book_selector),
            )
        }
        dropdown()
    }
}

@Composable
private fun BookPickerDropdown(
    expanded: Boolean,
    books: List<com.plusmobileapps.chefmate.recipebook.data.RecipeBook>,
    activeBookId: Long?,
    onDismiss: () -> Unit,
    onBookSelected: (Long) -> Unit,
    onEditBook: (Long) -> Unit,
    onCreateBook: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        books.forEach { book ->
            DropdownMenuItem(
                text = { Text(book.name) },
                onClick = { onBookSelected(book.id) },
                leadingIcon = {
                    if (book.id == activeBookId) {
                        Icon(Icons.Default.Check, contentDescription = null)
                    } else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
                },
                trailingIcon = {
                    IconButton(onClick = { onEditBook(book.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.recipe_list_book_edit),
                        )
                    }
                },
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(Res.string.recipe_list_book_create)) },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
            onClick = onCreateBook,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookPickerSheet(
    books: List<com.plusmobileapps.chefmate.recipebook.data.RecipeBook>,
    activeBookId: Long?,
    onDismiss: () -> Unit,
    onBookSelected: (Long) -> Unit,
    onEditBook: (Long) -> Unit,
    onCreateBook: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Text(
                text = stringResource(Res.string.recipe_list_book_picker_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(ChefMateTheme.dimens.paddingNormal),
            )
            books.forEach { book ->
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { onBookSelected(book.id) }
                            .padding(
                                horizontal = ChefMateTheme.dimens.paddingNormal,
                                vertical = ChefMateTheme.dimens.paddingSmall,
                            ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            if (book.id == activeBookId) Icons.Default.Check
                            else Icons.Outlined.Circle,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(ChefMateTheme.dimens.paddingNormal))
                    Text(text = book.name, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onEditBook(book.id) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.recipe_list_book_edit),
                        )
                    }
                }
            }
            DropdownRowCreate(onCreateBook = onCreateBook)
            Spacer(modifier = Modifier.height(ChefMateTheme.dimens.paddingNormal))
        }
    }
}

@Composable
private fun DropdownRowCreate(onCreateBook: () -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onCreateBook)
                .padding(
                    horizontal = ChefMateTheme.dimens.paddingNormal,
                    vertical = ChefMateTheme.dimens.paddingSmall,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(ChefMateTheme.dimens.paddingNormal))
        Text(text = stringResource(Res.string.recipe_list_book_create))
    }
}

// region Sort & Filter Bottom Sheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SortFilterBottomSheet(
    currentSort: RecipeSortOption,
    activeFilters: Set<RecipeFilterOption>,
    activeCategories: Set<BuiltinCategory>,
    activeUserCategoryIds: Set<Long>,
    availableUserCategories: List<com.plusmobileapps.chefmate.recipe.data.Category>,
    onApply:
        (
            sort: RecipeSortOption,
            filters: Set<RecipeFilterOption>,
            categories: Set<BuiltinCategory>,
            userCategoryIds: Set<Long>,
        ) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        SortFilterSheetContent(
            initialSort = currentSort,
            initialFilters = activeFilters,
            initialCategories = activeCategories,
            initialUserCategoryIds = activeUserCategoryIds,
            availableUserCategories = availableUserCategories,
            onApply = onApply,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SortFilterSheetContent(
    initialSort: RecipeSortOption,
    initialFilters: Set<RecipeFilterOption>,
    initialCategories: Set<BuiltinCategory>,
    initialUserCategoryIds: Set<Long> = emptySet(),
    availableUserCategories: List<com.plusmobileapps.chefmate.recipe.data.Category> = emptyList(),
    onApply:
        (
            sort: RecipeSortOption,
            filters: Set<RecipeFilterOption>,
            categories: Set<BuiltinCategory>,
            userCategoryIds: Set<Long>,
        ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedSort by remember { mutableStateOf(initialSort) }
    var selectedFilters by remember { mutableStateOf(initialFilters) }
    var selectedCategories by remember { mutableStateOf(initialCategories) }
    var selectedUserCategoryIds by remember { mutableStateOf(initialUserCategoryIds) }
    val anyFilterActive =
        selectedFilters.isNotEmpty() ||
            selectedCategories.isNotEmpty() ||
            selectedUserCategoryIds.isNotEmpty()

    Column(modifier = modifier.padding(horizontal = 16.dp).navigationBarsPadding()) {
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
            if (anyFilterActive) {
                TextButton(
                    onClick = {
                        selectedFilters = emptySet()
                        selectedCategories = emptySet()
                        selectedUserCategoryIds = emptySet()
                    }
                ) {
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

        // Filter by Category — presets first (in enum order) followed by user-created categories.
        Text(
            text = stringResource(Res.string.recipe_list_filter_by_category),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BuiltinCategory.entries.forEach { category ->
                FilterChip(
                    selected = category in selectedCategories,
                    onClick = {
                        selectedCategories =
                            if (category in selectedCategories) {
                                selectedCategories - category
                            } else {
                                selectedCategories + category
                            }
                    },
                    label = { Text(stringResource(category.labelRes())) },
                )
            }
            // User-created categories: filter out any that masquerade as a preset (by builtinId)
            // so we don't render a duplicate chip when both representations are present.
            availableUserCategories
                .filter { it.builtinId == null }
                .forEach { category ->
                    val isSelected = category.id in selectedUserCategoryIds
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedUserCategoryIds =
                                if (isSelected) selectedUserCategoryIds - category.id
                                else selectedUserCategoryIds + category.id
                        },
                        label = { Text(category.name) },
                    )
                }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                onApply(selectedSort, selectedFilters, selectedCategories, selectedUserCategoryIds)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.recipe_list_apply))
        }
        Spacer(Modifier.height(16.dp))
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

private fun BuiltinCategory.labelRes(): StringResource =
    when (this) {
        BuiltinCategory.BREAKFAST -> Res.string.recipe_list_category_breakfast
        BuiltinCategory.LUNCH -> Res.string.recipe_list_category_lunch
        BuiltinCategory.DINNER -> Res.string.recipe_list_category_dinner
        BuiltinCategory.APPETIZER -> Res.string.recipe_list_category_appetizer
        BuiltinCategory.SIDE -> Res.string.recipe_list_category_side
        BuiltinCategory.DESSERT -> Res.string.recipe_list_category_dessert
        BuiltinCategory.SNACK -> Res.string.recipe_list_category_snack
        BuiltinCategory.DRINK -> Res.string.recipe_list_category_drink
        BuiltinCategory.OTHER -> Res.string.recipe_list_category_other
        BuiltinCategory.AI -> Res.string.recipe_list_category_ai
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
    activeCategories: Set<BuiltinCategory>,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filterLabels =
        (activeFilters.map { stringResource(it.labelRes()) } +
                activeCategories.map { stringResource(it.labelRes()) })
            .joinToString(", ")

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
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    isSelectionMode: Boolean = false,
    selectedRecipeIds: Set<Long> = emptySet(),
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        state = state,
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding =
            PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 8.dp,
                bottom = 8.dp + bottomContentPadding,
            ),
    ) {
        items(recipes.size, key = { recipes[it].id }) { index ->
            val recipe = recipes[index]
            RecipeGridItem(
                recipe = recipe,
                onClick = { onRecipeClicked(recipe) },
                isSelectionMode = isSelectionMode,
                isSelected = recipe.id in selectedRecipeIds,
            )
        }
    }
}

@Composable
private fun RecipeGridItem(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (isSelectionMode && isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
            ),
    ) {
        Box {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.2f),
            )
            if (isSelectionMode) {
                SelectionBadge(
                    isSelected = isSelected,
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                )
            }
        }
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

/**
 * Filled-circle checkmark when selected, hollow circle when not — small enough to overlay a recipe
 * image without overpowering it.
 */
@Composable
private fun SelectionBadge(isSelected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(24.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color =
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.Check else Icons.Outlined.Circle,
            contentDescription = null,
            tint =
                if (isSelected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(2.dp),
        )
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
    bottomContentPadding: androidx.compose.ui.unit.Dp = 0.dp,
    isSelectionMode: Boolean = false,
    selectedRecipeIds: Set<Long> = emptySet(),
) {
    LazyColumn(
        state = state,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = bottomContentPadding),
    ) {
        items(recipes.size, key = { recipes[it].id }) { index ->
            val recipe = recipes[index]
            RecipeListItemContent(
                recipe = recipe,
                onClick = { onRecipeClicked(recipe) },
                isSelectionMode = isSelectionMode,
                isSelected = recipe.id in selectedRecipeIds,
            )
        }
    }
}

/** Approximate height of the Continue/Done Cooking FAB stack plus breathing room. */
private val FabStackReserve = 152.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeListItemContent(
    recipe: RecipeListItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
) {
    val background =
        if (isSelectionMode && isSelected) MaterialTheme.colorScheme.primaryContainer
        else androidx.compose.ui.graphics.Color.Transparent
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .background(background)
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSelectionMode) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint =
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
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
