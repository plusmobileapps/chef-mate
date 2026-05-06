@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.plusmobileapps.chefmate.cook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chefmate.client.cook.public.generated.resources.Res
import chefmate.client.cook.public.generated.resources.cook_mode_allow_screen_off
import chefmate.client.cook.public.generated.resources.cook_mode_close
import chefmate.client.cook.public.generated.resources.cook_mode_directions
import chefmate.client.cook.public.generated.resources.cook_mode_ingredients
import chefmate.client.cook.public.generated.resources.cook_mode_keep_screen_on
import chefmate.client.cook.public.generated.resources.cook_mode_layout_split
import chefmate.client.cook.public.generated.resources.cook_mode_layout_stacked
import chefmate.client.cook.public.generated.resources.cook_mode_loading
import chefmate.client.cook.public.generated.resources.cook_mode_no_active_recipe
import chefmate.client.cook.public.generated.resources.cook_mode_whats_cooking
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.ui.KeepScreenOn
import com.plusmobileapps.chefmate.ui.components.WindowSizeClass
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun CookModeScreen(bloc: CookModeBloc, modifier: Modifier = Modifier) {
    val state by bloc.state.collectAsState()
    if (state.keepScreenOn) {
        KeepScreenOn()
    }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSizeClass =
                when {
                    maxWidth < 600.dp -> WindowSizeClass.COMPACT
                    maxWidth < 840.dp -> WindowSizeClass.MEDIUM
                    else -> WindowSizeClass.EXPANDED
                }
            val isCompactHeight = maxHeight < 480.dp
            if (windowSizeClass == WindowSizeClass.COMPACT) {
                CookModeMobileLayout(bloc = bloc, state = state, windowSizeClass = windowSizeClass)
            } else {
                CookModeTabletLayout(
                    bloc = bloc,
                    state = state,
                    windowSizeClass = windowSizeClass,
                    isCompactHeight = isCompactHeight,
                )
            }
        }
    }
}

@Composable
private fun CookModeTabletLayout(
    bloc: CookModeBloc,
    state: CookModeBloc.Model,
    windowSizeClass: WindowSizeClass,
    isCompactHeight: Boolean,
) {
    var showWhatsCooking by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        CookModeBody(
            isLoading = state.isLoading,
            recipe = state.activeRecipe,
            layoutMode = state.layoutMode,
            windowSizeClass = windowSizeClass,
            bottomReserve = if (isCompactHeight) 0.dp else BottomBarReserve,
            modifier = Modifier.fillMaxSize(),
        )
        CookModeAppBar(
            title = state.activeRecipe?.title.orEmpty(),
            layoutMode = state.layoutMode,
            keepScreenOn = state.keepScreenOn,
            onLayoutToggle = bloc::onLayoutToggled,
            onKeepScreenOnToggle = bloc::onKeepScreenOnToggled,
            onCloseClicked = bloc::onCloseClicked,
            onWhatsCookingClicked = if (isCompactHeight) ({ showWhatsCooking = true }) else null,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        if (!isCompactHeight) {
            CookModeBottomBar(
                chips = state.activeSessions,
                onWhatsCookingClicked = { showWhatsCooking = true },
                onChipClicked = bloc::onRecipeChipClicked,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    if (showWhatsCooking) {
        WhatsCookingModalSheet(
            bloc = bloc.whatsCookingBloc,
            onDismiss = { showWhatsCooking = false },
        )
    }
}

@Composable
private fun WhatsCookingModalSheet(bloc: WhatsCookingBloc, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
        WhatsCookingScreen(
            bloc = bloc,
            onClose = onDismiss,
            onRecipeSelected = onDismiss,
            modifier = Modifier.padding(bottom = navBarBottom),
        )
    }
}

@Composable
private fun CookModeMobileLayout(
    bloc: CookModeBloc,
    state: CookModeBloc.Model,
    windowSizeClass: WindowSizeClass,
) {
    val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)
    val scope = rememberCoroutineScope()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val gestureBottom = WindowInsets.systemGestures.asPaddingValues().calculateBottomPadding()
    // Reserve enough at the bottom that interactive sheet content sits clear of the gesture-nav
    // strip on devices using gesture nav (where navigationBars inset is ~0).
    val safeBottom = maxOf(navBarBottom, gestureBottom)
    val peekHeight = MobilePeekHeight + safeBottom

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = peekHeight,
        sheetContent = {
            CookingPeekRow(
                title = state.activeRecipe?.title,
                onIconClicked = { scope.launch { sheetState.expand() } },
            )
            HorizontalDivider()
            WhatsCookingScreen(
                bloc = bloc.whatsCookingBloc,
                onClose = { scope.launch { sheetState.partialExpand() } },
                onRecipeSelected = { scope.launch { sheetState.partialExpand() } },
                modifier = Modifier.padding(bottom = safeBottom),
            )
        },
    ) { bodyPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(bottom = bodyPadding.calculateBottomPadding())
        ) {
            CookModeBody(
                isLoading = state.isLoading,
                recipe = state.activeRecipe,
                layoutMode = state.layoutMode,
                windowSizeClass = windowSizeClass,
                bottomReserve = 0.dp,
                modifier = Modifier.fillMaxSize(),
            )
            CookModeAppBar(
                title = state.activeRecipe?.title.orEmpty(),
                layoutMode = state.layoutMode,
                keepScreenOn = state.keepScreenOn,
                onLayoutToggle = bloc::onLayoutToggled,
                onKeepScreenOnToggle = bloc::onKeepScreenOnToggled,
                onCloseClicked = bloc::onCloseClicked,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun CookingPeekRow(
    title: String?,
    onIconClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onIconClicked) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = stringResource(Res.string.cook_mode_whats_cooking),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = title ?: stringResource(Res.string.cook_mode_no_active_recipe),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CookModeAppBar(
    title: String,
    layoutMode: CookModeBloc.LayoutMode,
    keepScreenOn: Boolean,
    onLayoutToggle: () -> Unit,
    onKeepScreenOnToggle: () -> Unit,
    onCloseClicked: () -> Unit,
    onWhatsCookingClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier.fillMaxWidth(),
        windowInsets =
            WindowInsets.systemBars
                .union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        title = { Text(text = title, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onCloseClicked) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.cook_mode_close),
                )
            }
        },
        actions = {
            if (onWhatsCookingClicked != null) {
                IconButton(onClick = onWhatsCookingClicked) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = stringResource(Res.string.cook_mode_whats_cooking),
                    )
                }
            }
            IconButton(onClick = onKeepScreenOnToggle) {
                Icon(
                    imageVector =
                        if (keepScreenOn) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription =
                        stringResource(
                            if (keepScreenOn) Res.string.cook_mode_allow_screen_off
                            else Res.string.cook_mode_keep_screen_on
                        ),
                )
            }
            IconButton(onClick = onLayoutToggle) {
                Icon(
                    imageVector =
                        if (layoutMode == CookModeBloc.LayoutMode.Stacked) Icons.Default.ViewWeek
                        else Icons.Default.ViewAgenda,
                    contentDescription =
                        stringResource(
                            if (layoutMode == CookModeBloc.LayoutMode.Stacked) {
                                Res.string.cook_mode_layout_split
                            } else {
                                Res.string.cook_mode_layout_stacked
                            }
                        ),
                )
            }
        },
        colors =
            TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.85f)
            ),
    )
}

@Composable
private fun CookModeBody(
    isLoading: Boolean,
    recipe: Recipe?,
    layoutMode: CookModeBloc.LayoutMode,
    windowSizeClass: WindowSizeClass,
    bottomReserve: Dp,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> LoadingState(modifier)
        recipe == null -> EmptyState(modifier)
        else -> {
            // Strikethrough + highlight state lives at this level so it survives layout-mode
            // toggles for the same recipe; reset when the active recipe changes.
            val ingredientLines = remember(recipe.ingredients) { splitLines(recipe.ingredients) }
            val crossedOut =
                remember(recipe.id, recipe.ingredients) {
                    mutableStateListOf(*BooleanArray(ingredientLines.size) { false }.toTypedArray())
                }
            val directionParagraphs = remember(recipe.directions) { splitLines(recipe.directions) }
            var highlightedDirectionIndex by
                remember(recipe.id, recipe.directions) { mutableStateOf(-1) }
            val onDirectionToggled: (Int) -> Unit = { index ->
                highlightedDirectionIndex = if (highlightedDirectionIndex == index) -1 else index
            }

            Box(modifier = modifier) {
                when (layoutMode) {
                    CookModeBloc.LayoutMode.Stacked ->
                        StackedLayout(
                            ingredientLines = ingredientLines,
                            crossedOut = crossedOut,
                            directionParagraphs = directionParagraphs,
                            highlightedIndex = highlightedDirectionIndex,
                            onDirectionToggled = onDirectionToggled,
                            bottomReserve = bottomReserve,
                        )
                    CookModeBloc.LayoutMode.Split ->
                        when (windowSizeClass) {
                            WindowSizeClass.COMPACT ->
                                SplitCompactLayout(
                                    ingredientLines = ingredientLines,
                                    crossedOut = crossedOut,
                                    directionParagraphs = directionParagraphs,
                                    highlightedIndex = highlightedDirectionIndex,
                                    onDirectionToggled = onDirectionToggled,
                                    bottomReserve = bottomReserve,
                                )
                            WindowSizeClass.MEDIUM,
                            WindowSizeClass.EXPANDED ->
                                SplitWideLayout(
                                    ingredientLines = ingredientLines,
                                    crossedOut = crossedOut,
                                    directionParagraphs = directionParagraphs,
                                    highlightedIndex = highlightedDirectionIndex,
                                    onDirectionToggled = onDirectionToggled,
                                    bottomReserve = bottomReserve,
                                )
                        }
                }
            }
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    val padding = ChefMateTheme.dimens.paddingNormal
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(padding))
            Text(stringResource(Res.string.cook_mode_loading))
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(Res.string.cook_mode_no_active_recipe),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val AppBarHeight = 64.dp
private val BottomBarReserve = 96.dp

/**
 * Peek-state height of the persistent mobile sheet: enough room for the default drag handle (~48dp)
 * plus the IconButton + title row (~48dp). Anything larger leaves an awkward empty band between the
 * drag handle and the row content.
 */
private val MobilePeekHeight = 96.dp

/**
 * Cook Mode draws edge-to-edge with a floating top app bar (and on tablet a floating bottom bar),
 * so body padding must reserve room for those bars *plus* keep text out of any display cutout (e.g.
 * landscape camera hole on Pixel). On mobile the persistent bottom sheet handles its own spacing
 * via [BottomSheetScaffold]'s padding values, so the caller passes [bottomReserve] = 0.
 */
@Composable
private fun bodyContentPadding(bottomReserve: Dp): PaddingValues {
    val safe = WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues()
    val layoutDir = LocalLayoutDirection.current
    return PaddingValues(
        start = safe.calculateStartPadding(layoutDir),
        end = safe.calculateEndPadding(layoutDir),
        top = safe.calculateTopPadding() + AppBarHeight,
        bottom = safe.calculateBottomPadding() + bottomReserve,
    )
}

@Composable
private fun StackedLayout(
    ingredientLines: List<String>,
    crossedOut: SnapshotStateList<Boolean>,
    directionParagraphs: List<String>,
    highlightedIndex: Int,
    onDirectionToggled: (Int) -> Unit,
    bottomReserve: Dp,
) {
    val padding = ChefMateTheme.dimens.paddingNormal
    val contentPadding = bodyContentPadding(bottomReserve)
    val layoutDir = LocalLayoutDirection.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDir) + padding,
                end = contentPadding.calculateEndPadding(layoutDir) + padding,
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
    ) {
        stickyHeader(key = "ingredients_header") {
            CookSectionHeader(stringResource(Res.string.cook_mode_ingredients))
        }
        itemsIndexed(ingredientLines, key = { i, _ -> "ingredient_$i" }) { index, line ->
            IngredientRow(
                text = line,
                crossedOut = crossedOut[index],
                onClick = { crossedOut[index] = !crossedOut[index] },
            )
        }
        stickyHeader(key = "directions_header") {
            CookSectionHeader(stringResource(Res.string.cook_mode_directions))
        }
        itemsIndexed(directionParagraphs, key = { i, _ -> "direction_$i" }) { index, paragraph ->
            DirectionRow(
                text = paragraph,
                highlighted = highlightedIndex == index,
                onClick = { onDirectionToggled(index) },
            )
        }
    }
}

@Composable
private fun SplitCompactLayout(
    ingredientLines: List<String>,
    crossedOut: SnapshotStateList<Boolean>,
    directionParagraphs: List<String>,
    highlightedIndex: Int,
    onDirectionToggled: (Int) -> Unit,
    bottomReserve: Dp,
) {
    val padding = ChefMateTheme.dimens.paddingNormal
    val contentPadding = bodyContentPadding(bottomReserve)
    val layoutDir = LocalLayoutDirection.current
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val tabs =
        listOf(
            stringResource(Res.string.cook_mode_ingredients),
            stringResource(Res.string.cook_mode_directions),
        )

    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDir),
                    end = contentPadding.calculateEndPadding(layoutDir),
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                )
    ) {
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
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = padding),
                verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
            ) {
                when (page) {
                    0 ->
                        ingredientLines.forEachIndexed { index, line ->
                            IngredientRow(
                                text = line,
                                crossedOut = crossedOut[index],
                                onClick = { crossedOut[index] = !crossedOut[index] },
                            )
                        }
                    1 ->
                        directionParagraphs.forEachIndexed { index, paragraph ->
                            DirectionRow(
                                text = paragraph,
                                highlighted = highlightedIndex == index,
                                onClick = { onDirectionToggled(index) },
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun SplitWideLayout(
    ingredientLines: List<String>,
    crossedOut: SnapshotStateList<Boolean>,
    directionParagraphs: List<String>,
    highlightedIndex: Int,
    onDirectionToggled: (Int) -> Unit,
    bottomReserve: Dp,
) {
    val padding = ChefMateTheme.dimens.paddingNormal
    val contentPadding = bodyContentPadding(bottomReserve)
    val layoutDir = LocalLayoutDirection.current
    var ingredientsWeight by remember { mutableStateOf(0.5f) }

    Row(
        modifier =
            Modifier.fillMaxSize()
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDir),
                    end = contentPadding.calculateEndPadding(layoutDir),
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                )
    ) {
        LazyColumn(
            modifier = Modifier.weight(ingredientsWeight).fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = padding),
            verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
        ) {
            stickyHeader(key = "ingredients_header") {
                CookSectionHeader(stringResource(Res.string.cook_mode_ingredients))
            }
            itemsIndexed(ingredientLines, key = { i, _ -> "ingredient_$i" }) { index, line ->
                IngredientRow(
                    text = line,
                    crossedOut = crossedOut[index],
                    onClick = { crossedOut[index] = !crossedOut[index] },
                )
            }
        }
        VerticalDragHandle(
            onDrag = { dx ->
                val deltaWeight = dx * 0.001f
                ingredientsWeight = (ingredientsWeight + deltaWeight).coerceIn(0.2f, 0.8f)
            }
        )
        LazyColumn(
            modifier = Modifier.weight(1f - ingredientsWeight).fillMaxHeight(),
            contentPadding = PaddingValues(horizontal = padding),
            verticalArrangement = Arrangement.spacedBy(ChefMateTheme.dimens.paddingExtraSmall),
        ) {
            stickyHeader(key = "directions_header") {
                CookSectionHeader(stringResource(Res.string.cook_mode_directions))
            }
            itemsIndexed(directionParagraphs, key = { i, _ -> "direction_$i" }) { index, paragraph
                ->
                DirectionRow(
                    text = paragraph,
                    highlighted = highlightedIndex == index,
                    onClick = { onDirectionToggled(index) },
                )
            }
        }
    }
}

@Composable
private fun VerticalDragHandle(onDrag: (Float) -> Unit) {
    Box(
        modifier =
            Modifier.width(16.dp).fillMaxHeight().pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier.width(4.dp)
                    .height(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small,
                    )
        )
    }
}

@Composable
private fun CookSectionHeader(title: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
        )
        HorizontalDivider()
    }
}

@Composable
private fun IngredientRow(text: String, crossedOut: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
        textDecoration = if (crossedOut) TextDecoration.LineThrough else TextDecoration.None,
        color =
            if (crossedOut) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        modifier =
            Modifier.fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = ChefMateTheme.dimens.paddingExtraSmall),
    )
}

@Composable
private fun DirectionRow(text: String, highlighted: Boolean, onClick: () -> Unit) {
    val dimens = ChefMateTheme.dimens
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
        modifier =
            Modifier.fillMaxWidth()
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
                .padding(vertical = dimens.paddingExtraSmall, horizontal = dimens.paddingExtraSmall),
    )
}

@Composable
private fun CookModeBottomBar(
    chips: List<CookModeBloc.Model.Chip>,
    onWhatsCookingClicked: () -> Unit,
    onChipClicked: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        windowInsets =
            WindowInsets.systemBars
                .union(WindowInsets.displayCutout)
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
    ) {
        IconButton(onClick = onWhatsCookingClicked) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = stringResource(Res.string.cook_mode_whats_cooking),
            )
        }
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            items(chips, key = { it.recipeId }) { chip ->
                FilterChip(
                    selected = chip.isActive,
                    onClick = { onChipClicked(chip.recipeId) },
                    label = { Text(chip.title, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(),
                )
            }
        }
    }
}

private fun splitLines(text: String): List<String> = text.split("\n").filter { it.isNotBlank() }
