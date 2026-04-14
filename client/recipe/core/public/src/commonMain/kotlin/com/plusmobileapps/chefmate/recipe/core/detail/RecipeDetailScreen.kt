@file:OptIn(ExperimentalTime::class, ExperimentalMaterial3ExpressiveApi::class)

package com.plusmobileapps.chefmate.recipe.core.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_add_favorite
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_calories
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_cook_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_created
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_cancel
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_confirm
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_message
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_delete_title
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_deleting_message
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_deleting_title
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_description
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_details
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_directions
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_edit
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_ingredients
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_kcal
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_minutes
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_prep_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_remove_favorite
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_servings
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_source
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_timestamps
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_total_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_updated
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListScreen
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.PlusResponsiveContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.components.WindowSizeClass
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    bloc: RecipeDetailBloc,
    modifier: Modifier = Modifier,
) {
    val state by bloc.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Delete confirmation dialog
    if (state.showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            recipeName = state.recipe.title,
            onConfirm = bloc::onDeleteConfirmed,
            onDismiss = bloc::onDeleteDismissed,
        )
    }

    // Deleting progress dialog
    if (state.isDeleting) {
        DeletingDialog()
    }

    // Add to Grocery List Bottom Sheet
    RecipeDetailSheet(bloc = bloc, sheetState = sheetState)

    var metadataCollapsed by rememberSaveable { mutableStateOf(false) }

    PlusResponsiveContainer(modifier = modifier.fillMaxSize()) { windowSizeClass ->
        val isCompact = windowSizeClass == WindowSizeClass.COMPACT
        val showToolbar = isCompact || !metadataCollapsed
        PlusHeaderContainer(
            modifier = Modifier.fillMaxSize(),
            data =
                PlusHeaderData.Child(
                    title = state.recipe.title.asTextData(),
                    onBackClick = bloc::onBackClicked,
                ),
            verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
            scrollEnabled = false,
            maxContentWidth = if (isCompact) 600.dp else Dp.Unspecified,
            floatingToolbar =
                if (showToolbar) {
                    {
                        HorizontalFloatingToolbar(
                            expanded = true,
                            floatingActionButton = {
                                FloatingActionButton(
                                    onClick = bloc::onAddToGroceryListClicked,
                                    shape = ChefMateTheme.shapes.large,
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, null)
                                }
                            },
                        ) {
                            IconButton(onClick = { bloc.onFavoriteToggled() }) {
                                Icon(
                                    imageVector =
                                        if (state.recipe.isFavorite) {
                                            Icons.Default.Favorite
                                        } else {
                                            Icons.Default.FavoriteBorder
                                        },
                                    contentDescription =
                                        if (state.recipe.isFavorite) {
                                            stringResource(Res.string.recipe_detail_remove_favorite)
                                        } else {
                                            stringResource(Res.string.recipe_detail_add_favorite)
                                        },
                                )
                            }
                            IconButton(onClick = { bloc.onEditClicked() }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(Res.string.recipe_detail_edit),
                                )
                            }
                            IconButton(onClick = { bloc.onDeleteClicked() }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(Res.string.recipe_detail_delete),
                                )
                            }
                        }
                    }
                } else {
                    null
                },
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    PlusLoadingIndicator()
                }
            } else if (isCompact) {
                RecipeDetailCompactContent(
                    recipe = state.recipe,
                    createdAt = state.createdAt,
                    updatedAt = state.updatedAt,
                    modifier = Modifier.weight(1f),
                )
            } else {
                RecipeDetailExpandedContent(
                    recipe = state.recipe,
                    createdAt = state.createdAt,
                    updatedAt = state.updatedAt,
                    metadataCollapsed = metadataCollapsed,
                    onMetadataCollapsedChange = { metadataCollapsed = it },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailSheet(
    bloc: RecipeDetailBloc,
    sheetState: androidx.compose.material3.SheetState,
) {
    val slot = bloc.childSlot.subscribeAsState()
    val child = slot.value.child?.instance as? RecipeDetailBloc.Sheet.AddToGroceryList

    // Remember the last active child so the sheet stays in composition during dismiss animation
    var sheetChild by remember { mutableStateOf(child) }
    if (child != null) {
        sheetChild = child
    }

    // When the bloc dismisses programmatically, animate the sheet hide before removing it
    LaunchedEffect(child) {
        if (child == null && sheetChild != null) {
            sheetState.hide()
            sheetChild = null
        }
    }

    if (sheetChild != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetChild?.bloc?.onBackClicked() },
            sheetState = sheetState,
        ) {
            sheetChild?.let { AddRecipeToGroceryListScreen(it.bloc) }
        }
    }
}

/**
 * Compact layout: LazyColumn with metadata items that scroll away,
 * a sticky TabRow, and a HorizontalPager that fills remaining space.
 */
@Composable
private fun RecipeDetailCompactContent(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs =
        listOf(
            stringResource(Res.string.recipe_detail_ingredients),
            stringResource(Res.string.recipe_detail_directions),
        )
    val scope = rememberCoroutineScope()
    val padding = ChefMateTheme.dimens.paddingNormal

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = spacedBy(padding),
    ) {
        // Hero section: image + key details side by side
        item {
            RecipeHeroSection(
                recipe = recipe,
                createdAt = createdAt,
                updatedAt = updatedAt,
                modifier = Modifier.padding(horizontal = padding),
            )
        }

        // Description below hero
        recipe.description?.let { description ->
            item {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = padding),
                )
            }
        }

        // Sticky TabRow
        stickyHeader {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) },
                    )
                }
            }
        }

        // Pager content fills remaining space
        item {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillParentMaxHeight(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                when (page) {
                    0 ->
                        IngredientsContent(
                            ingredients = recipe.ingredients,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                        )
                    1 ->
                        DirectionsContent(
                            directions = recipe.directions,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                        )
                }
            }
        }
    }
}

/**
 * Tablet layout: 3-column layout with a collapsible/resizable metadata column on the left,
 * ingredients in the middle, and directions on the right.
 * All columns scroll independently with sticky headers on ingredients/directions.
 * The divider between ingredients and directions is also draggable to resize.
 */
@Composable
private fun ColumnScope.RecipeDetailExpandedContent(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    metadataCollapsed: Boolean,
    onMetadataCollapsedChange: (Boolean) -> Unit,
) {
    val padding = ChefMateTheme.dimens.paddingNormal
    val density = LocalDensity.current
    val bottomPadding =
        if (metadataCollapsed) PaddingValues() else PaddingValues(bottom = 80.dp)

    var metadataWidthDp by remember { mutableStateOf(240.dp) }
    val minMetadataWidth = 160.dp
    val maxMetadataWidth = 400.dp

    // Ratio for ingredients vs directions (0.0 = all directions, 1.0 = all ingredients)
    var ingredientsWeight by remember { mutableStateOf(0.5f) }

    Row(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = padding),
    ) {
        if (metadataCollapsed) {
            // Collapsed: narrow strip with expand button
            Column(
                modifier =
                    Modifier
                        .width(48.dp)
                        .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(onClick = { onMetadataCollapsedChange(false) }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                    )
                }
            }
        } else {
            // Column 1: Metadata (resizable)
            LazyColumn(
                modifier = Modifier.width(metadataWidthDp),
                verticalArrangement = spacedBy(padding),
                contentPadding = bottomPadding,
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(onClick = { onMetadataCollapsedChange(true) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                                contentDescription = null,
                            )
                        }
                    }
                }
                item {
                    RecipeImage(
                        imageUrl = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                    )
                }
                recipe.starRating?.let { rating ->
                    item { StarRating(rating = rating) }
                }
                recipe.description?.let { description ->
                    item {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item { DetailsCard(recipe = recipe) }
                recipe.sourceUrl?.let { sourceUrl ->
                    item { SourceUrlCard(sourceUrl = sourceUrl) }
                }
                item { TimestampsCard(createdAt = createdAt, updatedAt = updatedAt) }
            }

            // Drag handle between metadata and ingredients
            DragHandle(
                onDrag = { dragAmountX ->
                    val deltaDp = with(density) { dragAmountX.toDp() }
                    metadataWidthDp =
                        (metadataWidthDp + deltaDp).coerceIn(
                            minMetadataWidth,
                            maxMetadataWidth,
                        )
                },
            )
        }

        // Column 2: Ingredients
        LazyColumn(
            modifier = Modifier.weight(ingredientsWeight),
            verticalArrangement = spacedBy(padding),
            contentPadding = bottomPadding,
        ) {
            stickyHeader {
                StickyColumnHeader(
                    title = stringResource(Res.string.recipe_detail_ingredients),
                )
            }
            item {
                Text(
                    text = recipe.ingredients,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        // Drag handle between ingredients and directions
        DragHandle(
            onDrag = { dragAmountX ->
                val delta = dragAmountX * 0.001f
                ingredientsWeight = (ingredientsWeight + delta).coerceIn(0.2f, 0.8f)
            },
        )

        // Column 3: Directions
        LazyColumn(
            modifier = Modifier.weight(1f - ingredientsWeight),
            verticalArrangement = spacedBy(padding),
            contentPadding = bottomPadding,
        ) {
            stickyHeader {
                StickyColumnHeader(
                    title = stringResource(Res.string.recipe_detail_directions),
                )
            }
            item {
                Text(
                    text = recipe.directions,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun DragHandle(
    onDrag: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(16.dp)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x)
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        // Vertical pill indicator
        Box(
            modifier =
                Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small,
                    ),
        )
    }
}

@Composable
private fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            Icon(
                imageVector =
                    if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint =
                    if (index < rating) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    },
            )
        }
    }
}

@Composable
private fun DescriptionCard(
    description: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.recipe_detail_description),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DetailsCard(
    recipe: Recipe,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.recipe_detail_details),
                style = MaterialTheme.typography.titleMedium,
            )

            recipe.servings?.let { servings ->
                DetailRow(
                    icon = Icons.Default.Restaurant,
                    label = stringResource(Res.string.recipe_detail_servings),
                    value = "$servings",
                )
            }

            recipe.prepTime?.let { prepTime ->
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_prep_time),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_minutes,
                            "minutes" to FixedString(prepTime.toString()),
                        ).localized(),
                )
            }

            recipe.cookTime?.let { cookTime ->
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_cook_time),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_minutes,
                            "minutes" to FixedString(cookTime.toString()),
                        ).localized(),
                )
            }

            recipe.totalTime?.let { totalTime ->
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_total_time),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_minutes,
                            "minutes" to FixedString(totalTime.toString()),
                        ).localized(),
                )
            }

            recipe.calories?.let { calories ->
                DetailRow(
                    icon = Icons.Default.LocalFireDepartment,
                    label = stringResource(Res.string.recipe_detail_calories),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_kcal,
                            "calories" to FixedString(calories.toString()),
                        ).localized(),
                )
            }
        }
    }
}

@Composable
private fun SourceUrlCard(
    sourceUrl: String,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.recipe_detail_source),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = sourceUrl,
                modifier = Modifier.clickable { uriHandler.openUri(sourceUrl) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

/**
 * Compact hero: image on the left, key details stacked on the right.
 */
@Composable
private fun RecipeHeroSection(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier =
                Modifier
                    .width(140.dp)
                    .height(140.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Star Rating
            recipe.starRating?.let { rating ->
                StarRating(rating = rating)
            }

            // Key details inline
            recipe.servings?.let { servings ->
                DetailRow(
                    icon = Icons.Default.Restaurant,
                    label = stringResource(Res.string.recipe_detail_servings),
                    value = "$servings",
                )
            }
            recipe.prepTime?.let { prepTime ->
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_prep_time),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_minutes,
                            "minutes" to FixedString(prepTime.toString()),
                        ).localized(),
                )
            }
            recipe.cookTime?.let { cookTime ->
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_cook_time),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_minutes,
                            "minutes" to FixedString(cookTime.toString()),
                        ).localized(),
                )
            }
            recipe.totalTime?.let { totalTime ->
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_total_time),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_minutes,
                            "minutes" to FixedString(totalTime.toString()),
                        ).localized(),
                )
            }
            recipe.calories?.let { calories ->
                DetailRow(
                    icon = Icons.Default.LocalFireDepartment,
                    label = stringResource(Res.string.recipe_detail_calories),
                    value =
                        PhraseModel(
                            Res.string.recipe_detail_kcal,
                            "calories" to FixedString(calories.toString()),
                        ).localized(),
                )
            }

            // Source URL
            recipe.sourceUrl?.let { sourceUrl ->
                val uriHandler = LocalUriHandler.current
                Text(
                    text = sourceUrl,
                    modifier = Modifier.clickable { uriHandler.openUri(sourceUrl) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }

            // Timestamps inline
            Text(
                text =
                    stringResource(Res.string.recipe_detail_created) +
                        " " + createdAt.localized(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(Res.string.recipe_detail_updated) +
                        " " + updatedAt.localized(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimestampsCard(
    createdAt: TextData,
    updatedAt: TextData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.recipe_detail_timestamps),
                style = MaterialTheme.typography.titleMedium,
            )
            DetailRow(
                label = stringResource(Res.string.recipe_detail_created),
                value = createdAt.localized(),
            )
            DetailRow(
                label = stringResource(Res.string.recipe_detail_updated),
                value = updatedAt.localized(),
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun IngredientsContent(
    ingredients: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.recipe_detail_ingredients),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = ingredients,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DirectionsContent(
    directions: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.recipe_detail_directions),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = directions,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun StickyColumnHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    recipeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.recipe_detail_delete_title)) },
        text = {
            Text(
                PhraseModel(
                    Res.string.recipe_detail_delete_message,
                    "recipe_name" to FixedString(recipeName),
                ).localized(),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.recipe_detail_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.recipe_detail_delete_cancel))
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun DeletingDialog(modifier: Modifier = Modifier) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal while deleting */ },
        title = { Text(stringResource(Res.string.recipe_detail_deleting_title)) },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
                Text(stringResource(Res.string.recipe_detail_deleting_message))
            }
        },
        confirmButton = { },
        modifier = modifier,
    )
}

private val previewBloc =
    object : RecipeDetailBloc {
        override val state: StateFlow<RecipeDetailBloc.Model> =
            MutableStateFlow(
                RecipeDetailBloc.Model(
                    isLoading = false,
                    recipe =
                        Recipe(
                            id = 1L,
                            title = "Spaghetti Bolognese",
                            description = "A classic Italian pasta dish.",
                            imageUrl = null,
                            starRating = 4,
                            servings = 4,
                            prepTime = 15,
                            cookTime = 60,
                            totalTime = 75,
                            calories = 600,
                            sourceUrl = "https://example.com/spaghetti-bolognese",
                            ingredients =
                                """
                                - 400g spaghetti
                                - 2 tbsp olive oil
                                - 1 onion, chopped
                                - 2 garlic cloves, crushed
                                - 400g minced beef
                                - 800g canned tomatoes
                                - Salt and pepper to taste
                                """.trimIndent(),
                            directions =
                                """
                                1. Cook the spaghetti according to the package instructions.
                                2. Heat the olive oil in a pan and sauté the onion and garlic until soft.
                                3. Add the minced beef and cook until browned.
                                4. Stir in the canned tomatoes and simmer for 45 minutes.
                                5. Season with salt and pepper.
                                6. Serve the sauce over the cooked spaghetti.
                                """.trimIndent(),
                            createdAt = Instant.parse("2023-01-01T12:00:00Z"),
                            updatedAt = Instant.parse("2023-02-01T12:00:00Z"),
                        ),
                    createdAt = FixedString("January 1, 2023"),
                    updatedAt = FixedString("February 1, 2023"),
                    isDeleting = false,
                    showDeleteConfirmationDialog = false,
                ),
            )
        override val childSlot: Value<ChildSlot<*, RecipeDetailBloc.Sheet>> =
            MutableValue(
                ChildSlot<Any, RecipeDetailBloc.Sheet>(null),
            )

        override fun onEditClicked() {
            TODO("Not yet implemented")
        }

        override fun onDeleteClicked() {
            TODO("Not yet implemented")
        }

        override fun onDeleteConfirmed() {
            TODO("Not yet implemented")
        }

        override fun onDeleteDismissed() {
            TODO("Not yet implemented")
        }

        override fun onFavoriteToggled() {
            TODO("Not yet implemented")
        }

        override fun onAddToGroceryListClicked() {
            TODO("Not yet implemented")
        }

        override fun onBackClicked() {
            TODO("Not yet implemented")
        }
    }

@Preview(heightDp = 1100)
@Composable
private fun RecipeDetailContentPreview() {
    ChefMateTheme {
        RecipeDetailScreen(
            bloc = previewBloc,
        )
    }
}

@Preview(heightDp = 1100)
@Composable
private fun RecipeDetailContentDarkPreview() {
    ChefMateTheme(darkTheme = true) {
        RecipeDetailScreen(
            bloc = previewBloc,
        )
    }
}
