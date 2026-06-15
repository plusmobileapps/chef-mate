@file:OptIn(
    ExperimentalTime::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
)

package com.plusmobileapps.chefmate.recipe.core.impl.detail.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chefmate.client.recipe.core.public.generated.resources.Res
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_added
import chefmate.client.recipe.core.public.generated.resources.recipe_add_to_grocery_list_view
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_add_favorite
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_add_to_grocery
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_add_to_meal_plan
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_calories
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_cook_mode
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_cook_mode_onboarding
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_cook_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_copied_to_clipboard
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
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_prep_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_remove_favorite
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_servings
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_share_text
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_share_url
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_source
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_timestamps
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_total_time
import chefmate.client.recipe.core.public.generated.resources.recipe_detail_updated
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.letIfTrue
import com.plusmobileapps.chefmate.recipe.categories.pickerLabelRes
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailTestTags
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.IngredientSection
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.LocalAnimatedVisibilityScope
import com.plusmobileapps.chefmate.ui.LocalSecondaryAnimatedVisibilityScope
import com.plusmobileapps.chefmate.ui.LocalSharedTransitionScope
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.components.PlusLoadingIndicator
import com.plusmobileapps.chefmate.ui.components.PlusOnboardingTooltip
import com.plusmobileapps.chefmate.ui.components.PlusResponsiveContainer
import com.plusmobileapps.chefmate.ui.components.RecipeImage
import com.plusmobileapps.chefmate.ui.components.WindowSizeClass
import com.plusmobileapps.chefmate.ui.text.toInlineMarkdownAnnotatedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import com.plusmobileapps.chefmate.util.rememberShareLauncher
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(bloc: RecipeDetailBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val shareLauncher = rememberShareLauncher()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(Res.string.recipe_detail_copied_to_clipboard)
    val groceryAddedMessage = stringResource(Res.string.recipe_add_to_grocery_list_added)
    val groceryViewLabel = stringResource(Res.string.recipe_add_to_grocery_list_view)

    LaunchedEffect(state.showGroceryAddedSnackbar) {
        if (state.showGroceryAddedSnackbar) {
            val result =
                snackbarHostState.showSnackbar(
                    message = groceryAddedMessage,
                    actionLabel = groceryViewLabel,
                    duration = SnackbarDuration.Long,
                )
            when (result) {
                SnackbarResult.ActionPerformed -> bloc.onViewGroceryListClicked()
                SnackbarResult.Dismissed -> bloc.onGrocerySnackbarDismissed()
            }
        }
    }

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

    var showOverflowMenu by remember { mutableStateOf(false) }
    var metadataCollapsed by rememberSaveable { mutableStateOf(false) }

    val fullImageSlot by bloc.fullImageSlot.subscribeAsState()
    val fullImageActive = fullImageSlot.child?.instance as? RecipeDetailBloc.FullImage.Active

    // Self-contained shared-element scope for the body image ↔ full-screen image morph. This is
    // local to this screen (not the root) so the root stack keeps its predictive-back animation.
    SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
            AnimatedContent(
                targetState = fullImageActive,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(250)) },
                label = "recipe-detail-full-image",
                modifier = Modifier.fillMaxSize(),
            ) { fullImage ->
                if (fullImage != null) {
                    // Full-screen image registers in this AnimatedContent's AVS — pairing with the
                    // body image's *secondary* registration (LocalSecondaryAnimatedVisibilityScope)
                    // for the morph.
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        RecipeImageFullScreenScreen(
                            imageUrl = fullImage.imageUrl,
                            recipeId = fullImage.recipeId,
                            contentDescription = fullImage.title,
                            onDismiss = bloc::onCloseFullImage,
                        )
                    }
                } else {
                    // Body image exposes this AnimatedContent's AVS as a *secondary* scope so it
                    // can
                    // morph into the full-screen image.
                    CompositionLocalProvider(LocalSecondaryAnimatedVisibilityScope provides this) {
                        RecipeDetailBody(
                            bloc = bloc,
                            state = state,
                            showOverflowMenu = showOverflowMenu,
                            onShowOverflowMenuChange = { showOverflowMenu = it },
                            metadataCollapsed = metadataCollapsed,
                            onMetadataCollapsedChange = { metadataCollapsed = it },
                            snackbarHostState = snackbarHostState,
                            shareLauncher = shareLauncher,
                            copiedMessage = copiedMessage,
                            scope = scope,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailBody(
    bloc: RecipeDetailBloc,
    state: RecipeDetailBloc.Model,
    showOverflowMenu: Boolean,
    onShowOverflowMenuChange: (Boolean) -> Unit,
    metadataCollapsed: Boolean,
    onMetadataCollapsedChange: (Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    shareLauncher: (String) -> Boolean,
    copiedMessage: String,
    scope: CoroutineScope,
) {
    // First-run coach mark pointing at the cook-mode button. Shows once the recipe has loaded and
    // hides as soon as the user taps it (or the bubble). Kept in local saveable state for now —
    // wiring it to a persisted "seen" flag is a follow-up.
    var showCookModeTooltip by rememberSaveable { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize().testTag(RecipeDetailTestTags.SCREEN)) {
        PlusResponsiveContainer(modifier = Modifier.fillMaxSize()) { windowSizeClass ->
            val isCompact = windowSizeClass == WindowSizeClass.COMPACT
            val showToolbar = isCompact || !metadataCollapsed
            PlusHeaderContainer(
                modifier = Modifier.fillMaxSize(),
                data =
                    PlusHeaderData.Child(
                        title = state.recipe.title.asTextData(),
                        onBackClick = bloc::onBackClicked,
                        trailingAccessory =
                            PlusHeaderData.TrailingAccessory.Custom {
                                Box {
                                    IconButton(onClick = { onShowOverflowMenuChange(true) }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription =
                                                stringResource(Res.string.recipe_detail_edit),
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showOverflowMenu,
                                        onDismissRequest = { onShowOverflowMenuChange(false) },
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(stringResource(Res.string.recipe_detail_edit))
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Edit, contentDescription = null)
                                            },
                                            onClick = {
                                                onShowOverflowMenuChange(false)
                                                bloc.onEditClicked()
                                            },
                                        )
                                        state.recipe.sourceUrl?.let { url ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        stringResource(
                                                            Res.string.recipe_detail_share_url
                                                        )
                                                    )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Share,
                                                        contentDescription = null,
                                                    )
                                                },
                                                onClick = {
                                                    onShowOverflowMenuChange(false)
                                                    if (shareLauncher(url)) {
                                                        scope.launch {
                                                            snackbarHostState.showSnackbar(
                                                                copiedMessage
                                                            )
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(
                                                        Res.string.recipe_detail_share_text
                                                    )
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Default.Share, contentDescription = null)
                                            },
                                            onClick = {
                                                onShowOverflowMenuChange(false)
                                                if (
                                                    shareLauncher(formatRecipeAsText(state.recipe))
                                                ) {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            copiedMessage
                                                        )
                                                    }
                                                }
                                            },
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    stringResource(Res.string.recipe_detail_delete)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = null,
                                                )
                                            },
                                            onClick = {
                                                onShowOverflowMenuChange(false)
                                                bloc.onDeleteClicked()
                                            },
                                        )
                                    }
                                }
                            },
                    ),
                verticalArrangement = spacedBy(ChefMateTheme.dimens.paddingNormal),
                scrollEnabled = false,
                maxContentWidth = if (isCompact) 600.dp else Dp.Unspecified,
                floatingToolbar =
                    letIfTrue(showToolbar) {
                        {
                            HorizontalFloatingToolbar(
                                expanded = true,
                                floatingActionButton = {
                                    PlusOnboardingTooltip(
                                        text =
                                            Res.string.recipe_detail_cook_mode_onboarding
                                                .asTextData(),
                                        visible = showCookModeTooltip && !state.isLoading,
                                        onDismiss = { showCookModeTooltip = false },
                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                showCookModeTooltip = false
                                                bloc.onCookModeClicked()
                                            },
                                            shape = ChefMateTheme.shapes.large,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SoupKitchen,
                                                contentDescription =
                                                    stringResource(
                                                        Res.string.recipe_detail_cook_mode
                                                    ),
                                            )
                                        }
                                    }
                                },
                            ) {
                                IconButton(onClick = bloc::onAddToGroceryListClicked) {
                                    Icon(
                                        imageVector = Icons.Default.AddShoppingCart,
                                        contentDescription =
                                            stringResource(Res.string.recipe_detail_add_to_grocery),
                                    )
                                }
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
                                                stringResource(
                                                    Res.string.recipe_detail_remove_favorite
                                                )
                                            } else {
                                                stringResource(
                                                    Res.string.recipe_detail_add_favorite
                                                )
                                            },
                                    )
                                }
                                IconButton(onClick = { bloc.onAddToMealPlanClicked() }) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription =
                                            stringResource(
                                                Res.string.recipe_detail_add_to_meal_plan
                                            ),
                                    )
                                }
                            }
                        }
                    },
            ) {
                if (state.isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        PlusLoadingIndicator()
                    }
                } else {
                    // Height-aware layout pick: a phone in landscape (height < 500dp) gets a
                    // dedicated 2-column layout — condensed hero on the left, tabs + pager on
                    // the right — instead of the cramped stacked-compact or 3-col tablet view.
                    BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        val isLandscapePhone = maxHeight < 500.dp
                        Column(modifier = Modifier.fillMaxSize()) {
                            when {
                                isLandscapePhone ->
                                    RecipeDetailLandscapeContent(
                                        recipe = state.recipe,
                                        createdAt = state.createdAt,
                                        updatedAt = state.updatedAt,
                                        formattedPrepTime = state.formattedPrepTime,
                                        formattedCookTime = state.formattedCookTime,
                                        formattedTotalTime = state.formattedTotalTime,
                                        onSourceUrlClicked = bloc::onSourceUrlClicked,
                                        onImageClicked = bloc::onImageClicked,
                                    )
                                isCompact ->
                                    RecipeDetailCompactContent(
                                        recipe = state.recipe,
                                        createdAt = state.createdAt,
                                        updatedAt = state.updatedAt,
                                        formattedPrepTime = state.formattedPrepTime,
                                        formattedCookTime = state.formattedCookTime,
                                        formattedTotalTime = state.formattedTotalTime,
                                        onSourceUrlClicked = bloc::onSourceUrlClicked,
                                        onImageClicked = bloc::onImageClicked,
                                        modifier = Modifier.weight(1f),
                                    )
                                else ->
                                    RecipeDetailExpandedContent(
                                        recipe = state.recipe,
                                        createdAt = state.createdAt,
                                        updatedAt = state.updatedAt,
                                        formattedPrepTime = state.formattedPrepTime,
                                        formattedCookTime = state.formattedCookTime,
                                        formattedTotalTime = state.formattedTotalTime,
                                        onSourceUrlClicked = bloc::onSourceUrlClicked,
                                        onImageClicked = bloc::onImageClicked,
                                        metadataCollapsed = metadataCollapsed,
                                        onMetadataCollapsedChange = onMetadataCollapsedChange,
                                    )
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 96.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeDetailSheet(
    bloc: RecipeDetailBloc,
    sheetState: androidx.compose.material3.SheetState,
) {
    val slot = bloc.childSlot.subscribeAsState()
    val child = slot.value.child?.instance

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
            onDismissRequest = {
                when (val current = sheetChild) {
                    is RecipeDetailBloc.Sheet.AddToGroceryList -> current.bloc.onBackClicked()
                    null -> {}
                }
            },
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            dragHandle = {
                val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(statusBarTop))
                    BottomSheetDefaults.DragHandle()
                }
            },
        ) {
            sheetChild?.bloc?.Content()
        }
    }
}

/**
 * Compact layout: LazyColumn with metadata items that scroll away, a sticky TabRow, and
 * ingredients/directions items laid out directly in the LazyColumn so the whole screen scrolls
 * together — no nested scroll conflicts.
 */
@Composable
private fun RecipeDetailCompactContent(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    formattedPrepTime: TextData?,
    formattedCookTime: TextData?,
    formattedTotalTime: TextData?,
    onSourceUrlClicked: (String) -> Unit,
    onImageClicked: () -> Unit,
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

    val ingredientLines = remember(recipe.ingredients) { splitLines(recipe.ingredients) }
    val crossedOut =
        remember(recipe.ingredients) {
            mutableStateListOf(*BooleanArray(ingredientLines.size) { false }.toTypedArray())
        }
    var highlightedDirectionIndex by remember(recipe.directions) { mutableStateOf(-1) }

    // Track each page's natural height so we can set both pages to the taller one's height.
    // Without this, swiping between tabs of different lengths shifts the LazyColumn's content
    // size and the visible scroll position appears to jump.
    var ingredientsHeightPx by remember(recipe.ingredients) { mutableStateOf(0) }
    var directionsHeightPx by remember(recipe.directions) { mutableStateOf(0) }
    val density = LocalDensity.current
    val pagerMinHeight = with(density) { maxOf(ingredientsHeightPx, directionsHeightPx).toDp() }

    val navBarBottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = spacedBy(padding),
        contentPadding = PaddingValues(bottom = 96.dp + navBarBottom),
    ) {
        // Hero section: image + key details side by side
        item(key = "hero") {
            RecipeHeroSection(
                recipe = recipe,
                createdAt = createdAt,
                updatedAt = updatedAt,
                formattedPrepTime = formattedPrepTime,
                formattedCookTime = formattedCookTime,
                formattedTotalTime = formattedTotalTime,
                onSourceUrlClicked = onSourceUrlClicked,
                onImageClicked = onImageClicked,
                modifier = Modifier.padding(horizontal = padding),
            )
        }

        if (recipe.categories.isNotEmpty()) {
            item(key = "categories") {
                RecipeCategoriesRow(
                    categories = recipe.categories,
                    modifier = Modifier.padding(horizontal = padding),
                )
            }
        }

        // Description below hero
        recipe.description?.let { description ->
            item(key = "description") {
                Text(
                    text = description.toInlineMarkdownAnnotatedString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = padding),
                )
            }
        }

        // Sticky TabRow
        stickyHeader(key = "tab_row") {
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

        // HorizontalPager wrapping content height — no nested vertical scroll
        item(key = "pager") {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1,
            ) { page ->
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = pagerMinHeight)) {
                    when (page) {
                        0 ->
                            IngredientsContent(
                                lines = ingredientLines,
                                crossedOut = crossedOut,
                                modifier =
                                    Modifier.fillMaxWidth().onSizeChanged {
                                        ingredientsHeightPx = it.height
                                    },
                            )
                        1 ->
                            DirectionsContent(
                                directions = recipe.directions,
                                highlightedIndex = highlightedDirectionIndex,
                                onHighlightedIndexChanged = { highlightedDirectionIndex = it },
                                modifier =
                                    Modifier.fillMaxWidth().onSizeChanged {
                                        directionsHeightPx = it.height
                                    },
                            )
                    }
                }
            }
        }
    }
}

/**
 * Phone landscape layout: condensed hero (image + key details) scrolls on the left ~40%, sticky
 * tabs + ingredients/directions pager on the right ~60%. Triggered when the available height is
 * short (< 500dp), regardless of width — so it covers landscape phones whether they fall into
 * COMPACT (<600dp wide) or EXPANDED (>=840dp wide) by the width-only breakpoint.
 */
@Composable
private fun ColumnScope.RecipeDetailLandscapeContent(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    formattedPrepTime: TextData?,
    formattedCookTime: TextData?,
    formattedTotalTime: TextData?,
    onSourceUrlClicked: (String) -> Unit,
    onImageClicked: () -> Unit,
) {
    val padding = ChefMateTheme.dimens.paddingNormal
    val navBarBottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val toolbarClearance = 96.dp + navBarBottom

    val ingredientLines = remember(recipe.ingredients) { splitLines(recipe.ingredients) }
    val crossedOut =
        remember(recipe.ingredients) {
            mutableStateListOf(*BooleanArray(ingredientLines.size) { false }.toTypedArray())
        }
    var highlightedDirectionIndex by remember(recipe.directions) { mutableStateOf(-1) }

    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs =
        listOf(
            stringResource(Res.string.recipe_detail_ingredients),
            stringResource(Res.string.recipe_detail_directions),
        )
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = padding),
        horizontalArrangement = Arrangement.spacedBy(padding),
    ) {
        // Left ~40%: condensed hero in a vertically scrolling column
        Column(
            modifier = Modifier.weight(0.4f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                secondarySharedElementKey = "recipe-image-fullscreen-${recipe.id}",
                onClick = onImageClicked,
            )
            recipe.starRating?.let { rating -> StarRating(rating = rating) }
            recipe.servings?.let { servings ->
                DetailRow(
                    icon = Icons.Default.Restaurant,
                    label = stringResource(Res.string.recipe_detail_servings),
                    value = "$servings",
                )
            }
            formattedPrepTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_prep_time),
                    value = it.localized(),
                )
            }
            formattedCookTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_cook_time),
                    value = it.localized(),
                )
            }
            formattedTotalTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_total_time),
                    value = it.localized(),
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
                            )
                            .localized(),
                )
            }
            recipe.sourceUrl?.let { sourceUrl ->
                Text(
                    text = sourceUrl,
                    modifier = Modifier.fillMaxWidth().clickable { onSourceUrlClicked(sourceUrl) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
            if (recipe.categories.isNotEmpty()) {
                RecipeCategoriesRow(categories = recipe.categories)
            }
            Text(
                text =
                    stringResource(Res.string.recipe_detail_created) + " " + createdAt.localized(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(Res.string.recipe_detail_updated) + " " + updatedAt.localized(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(toolbarClearance))
        }

        // Right ~60%: sticky tab row + pager. Each page scrolls independently so swiping
        // between Ingredients and Directions does not affect the other tab's scroll position.
        Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1,
            ) { page ->
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    when (page) {
                        0 ->
                            IngredientsContent(
                                lines = ingredientLines,
                                crossedOut = crossedOut,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        1 ->
                            DirectionsContent(
                                directions = recipe.directions,
                                highlightedIndex = highlightedDirectionIndex,
                                onHighlightedIndexChanged = { highlightedDirectionIndex = it },
                                modifier = Modifier.fillMaxWidth(),
                            )
                    }
                    Spacer(modifier = Modifier.height(toolbarClearance))
                }
            }
        }
    }
}

/**
 * Tablet layout: 3-column layout with a collapsible/resizable metadata column on the left,
 * ingredients in the middle, and directions on the right. All columns scroll independently with
 * sticky headers on ingredients/directions. The divider between ingredients and directions is also
 * draggable to resize.
 */
@Composable
private fun ColumnScope.RecipeDetailExpandedContent(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    formattedPrepTime: TextData?,
    formattedCookTime: TextData?,
    formattedTotalTime: TextData?,
    onSourceUrlClicked: (String) -> Unit,
    onImageClicked: () -> Unit,
    metadataCollapsed: Boolean,
    onMetadataCollapsedChange: (Boolean) -> Unit,
) {
    val padding = ChefMateTheme.dimens.paddingNormal
    val density = LocalDensity.current
    val navBarBottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val toolbarClearance = 80.dp + navBarBottom

    var metadataWidthDp by remember { mutableStateOf(240.dp) }
    val minMetadataWidth = 160.dp
    val maxMetadataWidth = 400.dp

    // Ratio for ingredients vs directions (0.0 = all directions, 1.0 = all ingredients)
    var ingredientsWeight by remember { mutableStateOf(0.5f) }

    val ingredientLines = remember(recipe.ingredients) { splitLines(recipe.ingredients) }
    val ingredientCrossedOut =
        remember(recipe.ingredients) {
            mutableStateListOf(*BooleanArray(ingredientLines.size) { false }.toTypedArray())
        }
    val directionParagraphs = remember(recipe.directions) { directionSteps(recipe.directions) }
    var directionHighlightedIndex by remember(recipe.directions) { mutableStateOf(-1) }

    Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = padding)) {
        if (metadataCollapsed) {
            // Collapsed: narrow strip with expand button
            Column(
                modifier = Modifier.width(48.dp).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(onClick = { onMetadataCollapsedChange(false) }) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                }
            }
        } else {
            // Column 1: Metadata (resizable)
            LazyColumn(
                modifier = Modifier.width(metadataWidthDp),
                verticalArrangement = spacedBy(padding),
            ) {
                item(key = "collapse_button") {
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
                item(key = "image") {
                    RecipeImage(
                        imageUrl = recipe.imageUrl,
                        contentDescription = recipe.title,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        secondarySharedElementKey = "recipe-image-fullscreen-${recipe.id}",
                        onClick = onImageClicked,
                    )
                }
                recipe.starRating?.let { rating ->
                    item(key = "star_rating") { StarRating(rating = rating) }
                }
                if (recipe.categories.isNotEmpty()) {
                    item(key = "categories") { RecipeCategoriesRow(categories = recipe.categories) }
                }
                recipe.description?.let { description ->
                    item(key = "metadata_description") {
                        Text(
                            text = description.toInlineMarkdownAnnotatedString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                item(key = "details_card") {
                    DetailsCard(
                        recipe = recipe,
                        formattedPrepTime = formattedPrepTime,
                        formattedCookTime = formattedCookTime,
                        formattedTotalTime = formattedTotalTime,
                    )
                }
                recipe.sourceUrl?.let { sourceUrl ->
                    item(key = "source_url") {
                        SourceUrlCard(
                            sourceUrl = sourceUrl,
                            onSourceUrlClicked = onSourceUrlClicked,
                        )
                    }
                }
                item(key = "timestamps") {
                    TimestampsCard(createdAt = createdAt, updatedAt = updatedAt)
                }
                if (!metadataCollapsed) {
                    item(key = "metadata_spacer") {
                        Spacer(modifier = Modifier.height(toolbarClearance))
                    }
                }
            }

            // Drag handle between metadata and ingredients
            DragHandle(
                onDrag = { dragAmountX ->
                    val deltaDp = with(density) { dragAmountX.toDp() }
                    metadataWidthDp =
                        (metadataWidthDp + deltaDp).coerceIn(minMetadataWidth, maxMetadataWidth)
                }
            )
        }

        // Column 2: Ingredients
        LazyColumn(
            modifier = Modifier.weight(ingredientsWeight),
            verticalArrangement = spacedBy(padding),
        ) {
            stickyHeader(key = "ingredients_header") {
                StickyColumnHeader(title = stringResource(Res.string.recipe_detail_ingredients))
            }
            itemsIndexed(ingredientLines, key = { index, _ -> "ingredient_$index" }) { index, line
                ->
                IngredientLineItem(
                    text = line,
                    crossedOut = ingredientCrossedOut[index],
                    onClick = { ingredientCrossedOut[index] = !ingredientCrossedOut[index] },
                    modifier = Modifier.padding(horizontal = padding),
                )
            }
            if (!metadataCollapsed) {
                item(key = "ingredients_spacer") {
                    Spacer(modifier = Modifier.height(toolbarClearance))
                }
            }
        }

        // Drag handle between ingredients and directions
        DragHandle(
            onDrag = { dragAmountX ->
                val delta = dragAmountX * 0.001f
                ingredientsWeight = (ingredientsWeight + delta).coerceIn(0.2f, 0.8f)
            }
        )

        // Column 3: Directions
        LazyColumn(
            modifier = Modifier.weight(1f - ingredientsWeight),
            verticalArrangement = spacedBy(padding),
        ) {
            stickyHeader(key = "directions_header") {
                StickyColumnHeader(title = stringResource(Res.string.recipe_detail_directions))
            }
            itemsIndexed(directionParagraphs, key = { index, _ -> "direction_$index" }) {
                index,
                step ->
                DirectionLineItem(
                    text = step.text,
                    number = step.number,
                    highlighted = directionHighlightedIndex == index,
                    onClick = {
                        directionHighlightedIndex =
                            if (directionHighlightedIndex == index) -1 else index
                    },
                    modifier = Modifier.padding(horizontal = padding),
                )
            }
            if (!metadataCollapsed) {
                item(key = "directions_spacer") {
                    Spacer(modifier = Modifier.height(toolbarClearance))
                }
            }
        }
    }
}

@Composable
private fun DragHandle(onDrag: (Float) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier.width(16.dp).fillMaxHeight().pointerInput(Unit) {
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
                Modifier.width(4.dp)
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small,
                    )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCategoriesRow(categories: Set<Category>, modifier: Modifier = Modifier) {
    if (categories.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { category ->
            val label =
                BuiltinCategory.fromId(category.builtinId)?.let {
                    stringResource(it.pickerLabelRes())
                } ?: category.name
            CategoryChip(label = label)
        }
    }
}

@Composable
private fun CategoryChip(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StarRating(rating: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
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
private fun DescriptionCard(description: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.recipe_detail_description),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description.toInlineMarkdownAnnotatedString(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun DetailsCard(
    recipe: Recipe,
    formattedPrepTime: TextData?,
    formattedCookTime: TextData?,
    formattedTotalTime: TextData?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
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

            formattedPrepTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_prep_time),
                    value = it.localized(),
                )
            }

            formattedCookTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_cook_time),
                    value = it.localized(),
                )
            }

            formattedTotalTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_total_time),
                    value = it.localized(),
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
                            )
                            .localized(),
                )
            }
        }
    }
}

@Composable
private fun SourceUrlCard(
    sourceUrl: String,
    onSourceUrlClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
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
                modifier = Modifier.clickable { onSourceUrlClicked(sourceUrl) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

/** Compact hero: image on the left, key details stacked on the right. */
@Composable
private fun RecipeHeroSection(
    recipe: Recipe,
    createdAt: TextData,
    updatedAt: TextData,
    formattedPrepTime: TextData?,
    formattedCookTime: TextData?,
    formattedTotalTime: TextData?,
    onSourceUrlClicked: (String) -> Unit,
    onImageClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RecipeImage(
            imageUrl = recipe.imageUrl,
            contentDescription = recipe.title,
            modifier = Modifier.width(140.dp).height(140.dp),
            secondarySharedElementKey = "recipe-image-fullscreen-${recipe.id}",
            onClick = onImageClicked,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Star Rating
            recipe.starRating?.let { rating -> StarRating(rating = rating) }

            // Key details inline
            recipe.servings?.let { servings ->
                DetailRow(
                    icon = Icons.Default.Restaurant,
                    label = stringResource(Res.string.recipe_detail_servings),
                    value = "$servings",
                )
            }
            formattedPrepTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_prep_time),
                    value = it.localized(),
                )
            }
            formattedCookTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_cook_time),
                    value = it.localized(),
                )
            }
            formattedTotalTime?.let {
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = stringResource(Res.string.recipe_detail_total_time),
                    value = it.localized(),
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
                            )
                            .localized(),
                )
            }

            // Source URL
            recipe.sourceUrl?.let { sourceUrl ->
                Text(
                    text = sourceUrl,
                    modifier = Modifier.clickable { onSourceUrlClicked(sourceUrl) },
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
                    stringResource(Res.string.recipe_detail_created) + " " + createdAt.localized(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    stringResource(Res.string.recipe_detail_updated) + " " + updatedAt.localized(),
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
    Card(modifier = modifier.fillMaxWidth()) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
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
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun splitLines(text: String): List<String> = text.split("\n").filter { it.isNotBlank() }

private fun formatRecipeAsText(recipe: Recipe): String = buildString {
    appendLine(recipe.title)
    recipe.description?.let {
        appendLine()
        appendLine(it)
    }
    appendLine()
    appendLine("Ingredients:")
    appendLine(recipe.ingredients)
    appendLine()
    appendLine("Directions:")
    appendLine(recipe.directions)
    recipe.sourceUrl?.let {
        appendLine()
        append("Source: $it")
    }
}

@Composable
private fun IngredientLineItem(
    text: String,
    crossedOut: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (IngredientSection.isHeader(text)) {
        // Sub-section header (e.g. "For the sauce:") — bold and not crossable.
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(
                        top = ChefMateTheme.dimens.paddingSmall,
                        bottom = ChefMateTheme.dimens.paddingExtraSmall,
                    ),
        )
        return
    }
    Text(
        text = text.toInlineMarkdownAnnotatedString(),
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
        textDecoration = if (crossedOut) TextDecoration.LineThrough else TextDecoration.None,
        color =
            if (crossedOut) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = ChefMateTheme.dimens.paddingExtraSmall),
    )
}

/**
 * One rendered directions line. [number] is the step number (1-based, reset per section); a null
 * [number] marks a section header (a line ending in `:`), which renders bold and is not numbered or
 * tappable.
 */
@Composable
private fun DirectionLineItem(
    text: String,
    number: Int?,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (number == null) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(
                        top = ChefMateTheme.dimens.paddingSmall,
                        bottom = ChefMateTheme.dimens.paddingExtraSmall,
                    ),
        )
        return
    }
    val dimens = ChefMateTheme.dimens
    val rendered =
        remember(text, number) {
            buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("$number. ") }
                append(text.toInlineMarkdownAnnotatedString())
            }
        }
    Text(
        text = rendered,
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .then(
                    if (highlighted) {
                        Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(dimens.paddingSmall),
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(
                    vertical = dimens.paddingExtraSmall,
                    horizontal = dimens.paddingExtraSmall,
                ),
    )
}

/**
 * A directions line with its display info: section headers (lines ending in `:`) carry a null
 * [number]; real steps are numbered 1-based and the counter resets after each header. Any manual
 * leading enumerator the author typed (e.g. "1. ") is stripped so we don't double-number.
 */
internal data class DirectionStep(val text: String, val number: Int?)

private val leadingEnumerator = Regex("""^\s*\d+[.)]\s+""")

internal fun directionSteps(directions: String): List<DirectionStep> {
    var counter = 0
    return splitLines(directions).map { line ->
        if (IngredientSection.isHeader(line)) {
            counter = 0
            DirectionStep(text = line, number = null)
        } else {
            counter += 1
            DirectionStep(text = line.replaceFirst(leadingEnumerator, ""), number = counter)
        }
    }
}

@Composable
private fun IngredientsContent(
    lines: List<String>,
    crossedOut: SnapshotStateList<Boolean>,
    modifier: Modifier = Modifier,
) {
    val dimens = ChefMateTheme.dimens
    Column(
        modifier = modifier.padding(dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingExtraSmall),
    ) {
        Text(
            text = stringResource(Res.string.recipe_detail_ingredients),
            style = MaterialTheme.typography.titleMedium,
        )
        lines.forEachIndexed { index, line ->
            IngredientLineItem(
                text = line,
                crossedOut = crossedOut[index],
                onClick = { crossedOut[index] = !crossedOut[index] },
            )
        }
    }
}

@Composable
private fun DirectionsContent(
    directions: String,
    highlightedIndex: Int,
    onHighlightedIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val steps = remember(directions) { directionSteps(directions) }

    val dimens = ChefMateTheme.dimens
    Column(
        modifier = modifier.padding(dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
    ) {
        Text(
            text = stringResource(Res.string.recipe_detail_directions),
            style = MaterialTheme.typography.titleMedium,
        )
        steps.forEachIndexed { index, step ->
            DirectionLineItem(
                text = step.text,
                number = step.number,
                highlighted = highlightedIndex == index,
                onClick = {
                    onHighlightedIndexChanged(if (highlightedIndex == index) -1 else index)
                },
            )
        }
    }
}

@Composable
private fun StickyColumnHeader(title: String, modifier: Modifier = Modifier) {
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
                    )
                    .localized()
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
        confirmButton = {},
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
                                """
                                    .trimIndent(),
                            directions =
                                """
                                1. Cook the spaghetti according to the package instructions.
                                2. Heat the olive oil in a pan and sauté the onion and garlic until soft.
                                3. Add the minced beef and cook until browned.
                                4. Stir in the canned tomatoes and simmer for 45 minutes.
                                5. Season with salt and pepper.
                                6. Serve the sauce over the cooked spaghetti.
                                """
                                    .trimIndent(),
                            categories =
                                setOf(
                                    Category(
                                        id = 1L,
                                        name = "Dinner",
                                        builtinId = BuiltinCategory.DINNER.id,
                                    ),
                                    Category(id = 100L, name = "Weeknight"),
                                ),
                            createdAt = Instant.parse("2023-01-01T12:00:00Z"),
                            updatedAt = Instant.parse("2023-02-01T12:00:00Z"),
                        ),
                    createdAt = FixedString("January 1, 2023"),
                    updatedAt = FixedString("February 1, 2023"),
                    isDeleting = false,
                    showDeleteConfirmationDialog = false,
                )
            )
        override val childSlot: Value<ChildSlot<*, RecipeDetailBloc.Sheet>> =
            MutableValue(ChildSlot<Any, RecipeDetailBloc.Sheet>(null))

        override val fullImageSlot: Value<ChildSlot<*, RecipeDetailBloc.FullImage>> =
            MutableValue(ChildSlot<Any, RecipeDetailBloc.FullImage>(null))

        override fun onImageClicked() {
            // Preview no-op
        }

        override fun onCloseFullImage() {
            // Preview no-op
        }

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

        override fun onAddToMealPlanClicked() {
            TODO("Not yet implemented")
        }

        override fun onCookModeClicked() {
            TODO("Not yet implemented")
        }

        override fun onSourceUrlClicked(url: String) {
            TODO("Not yet implemented")
        }

        override fun onDismissSheet() {
            TODO("Not yet implemented")
        }

        override fun onViewGroceryListClicked() {
            TODO("Not yet implemented")
        }

        override fun onGrocerySnackbarDismissed() {
            TODO("Not yet implemented")
        }

        override fun onBackClicked() {
            TODO("Not yet implemented")
        }

        @Composable override fun Content(modifier: Modifier) = RecipeDetailScreen(this, modifier)
    }

@Preview(heightDp = 1100)
@Composable
private fun RecipeDetailContentPreview() {
    ChefMateTheme { RecipeDetailScreen(bloc = previewBloc) }
}

@Preview(heightDp = 1100)
@Composable
private fun RecipeDetailContentDarkPreview() {
    ChefMateTheme(darkTheme = true) { RecipeDetailScreen(bloc = previewBloc) }
}
