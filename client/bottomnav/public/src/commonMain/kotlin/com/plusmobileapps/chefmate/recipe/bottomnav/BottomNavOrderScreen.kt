package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import chefmate.client.bottomnav.public.generated.resources.Res
import chefmate.client.bottomnav.public.generated.resources.bottom_nav_order_drag_handle_content_description
import chefmate.client.bottomnav.public.generated.resources.bottom_nav_order_save
import chefmate.client.bottomnav.public.generated.resources.bottom_nav_order_title
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusFloatingActionButton
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun BottomNavOrderScreen(
    bloc: BottomNavOrderBloc,
    modifier: Modifier = Modifier,
    // When false the header drops the back arrow — used when the screen is rendered as the always-
    // visible detail pane inside a master-detail layout, where a back button would be misleading
    // (the user can never "leave" the pane in that mode).
    showBackButton: Boolean = true,
    // Defaults to the PlusHeaderContainer's standard centered-600dp content width. Pass
    // `Dp.Unspecified` when the screen is rendered as a fixed-width pane (e.g. inside the
    // settings master-detail layout) so the content fills the pane without internal whitespace.
    maxContentWidth: androidx.compose.ui.unit.Dp = 600.dp,
) {
    val state by bloc.state.collectAsState()

    val headerData =
        if (showBackButton) {
            PlusHeaderData.Child(
                title = Res.string.bottom_nav_order_title.asTextData(),
                onBackClick = bloc::onBack,
            )
        } else {
            PlusHeaderData.Parent(title = Res.string.bottom_nav_order_title.asTextData())
        }

    PlusHeaderContainer(
        modifier = modifier,
        scrollEnabled = false,
        maxContentWidth = maxContentWidth,
        data = headerData,
        floatingActionButton = {
            // Hide the Save FAB until something has actually been reordered. AnimatedVisibility
            // gives it a quick fade+scale entrance so the affordance doesn't pop in abruptly on
            // the first move.
            AnimatedVisibility(
                visible = state.hasUnsavedChanges,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                PlusFloatingActionButton(
                    text = stringResource(Res.string.bottom_nav_order_save),
                    icon = Icons.Default.Check,
                    onClick = bloc::onSave,
                )
            }
        },
    ) {
        ReorderableTabList(
            tabs = state.editedOrder,
            onMove = bloc::onMove,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun ReorderableTabList(
    tabs: List<BottomNavBloc.Tab>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var draggingId by remember { mutableStateOf<BottomNavBloc.Tab?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }

    LazyColumn(state = listState, modifier = modifier) {
        itemsIndexed(items = tabs, key = { _, tab -> tab.name }) { _, tab ->
            val isDragging = tab == draggingId
            ReorderableTabRow(
                tab = tab,
                isDragging = isDragging,
                offsetY = if (isDragging) dragOffsetY else 0f,
                // Animate placement for non-dragged rows so they slide into the slot the dragged
                // row vacates. Skip it for the dragged row itself — its position is driven by a
                // manual translationY and offset compensation in `onDrag`, so an animated
                // placement would fight that and visually lag the finger.
                modifier = if (isDragging) Modifier else Modifier.animateItem(),
                onDragStart = {
                    draggingId = tab
                    dragOffsetY = 0f
                },
                onDrag = { delta ->
                    dragOffsetY += delta
                    val rowHeight = listState.rowHeightFor(tab) ?: return@ReorderableTabRow
                    val draggedIndex =
                        tabs.indexOf(draggingId).takeIf { it >= 0 } ?: return@ReorderableTabRow
                    val rowsCrossed = (dragOffsetY / rowHeight).toInt()
                    if (rowsCrossed != 0) {
                        val target = (draggedIndex + rowsCrossed).coerceIn(0, tabs.lastIndex)
                        if (target != draggedIndex) {
                            onMove(draggedIndex, target)
                            // The dragged row has visually moved by `rowsCrossed * rowHeight`
                            // pixels because the underlying list rebuilt. Subtract that distance
                            // from the running offset so the visual offset stays continuous with
                            // the user's finger.
                            dragOffsetY -= (target - draggedIndex) * rowHeight
                        }
                    }
                },
                onDragEnd = {
                    draggingId = null
                    dragOffsetY = 0f
                },
            )
        }
    }
}

@Composable
private fun ReorderableTabRow(
    tab: BottomNavBloc.Tab,
    isDragging: Boolean,
    offsetY: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dragDescription =
        stringResource(Res.string.bottom_nav_order_drag_handle_content_description)
    // `pointerInput(tab)` is keyed by the tab, which is stable across reorders (LazyColumn keys by
    // `tab.name`). That means the gesture coroutine never restarts and would otherwise capture the
    // first `onDrag`/`onDragStart`/`onDragEnd` lambdas forever — which in turn close over the
    // original `tabs` list from the parent. Route through `rememberUpdatedState` so each gesture
    // event invokes the latest lambda (and the freshest `tabs` snapshot it captures).
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    // Spring between the resting list-row look and the lifted card look so picking up / dropping
    // a tab feels physical instead of snapping. `animate*AsState` starts at the target on first
    // composition, so rest-state screenshots remain unchanged.
    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "drag-elevation")
    val cornerRadius by animateDpAsState(if (isDragging) 12.dp else 0.dp, label = "drag-corner")
    val scale by animateFloatAsState(if (isDragging) 0.96f else 1f, label = "drag-scale")
    Surface(
        tonalElevation = elevation,
        shadowElevation = elevation,
        shape = RoundedCornerShape(cornerRadius),
        modifier =
            modifier
                .fillMaxWidth()
                // LazyColumn paints items in source order, so a row dragged downward would render
                // beneath the rows it overlaps. Lifting `zIndex` keeps the dragged row on top in
                // either direction.
                .zIndex(if (isDragging) 1f else 0f)
                .graphicsLayer {
                    translationY = offsetY
                    scaleX = scale
                    scaleY = scale
                },
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(ChefMateTheme.dimens.rowHeight)
                    .padding(horizontal = ChefMateTheme.dimens.paddingNormal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = tab.getIcon(), contentDescription = null)
            Spacer(modifier = Modifier.width(ChefMateTheme.dimens.paddingNormal))
            Text(
                text = stringResource(tab.getLabel()),
                style = ChefMateTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier =
                    Modifier.size(48.dp)
                        .semantics { contentDescription = dragDescription }
                        .pointerInput(tab) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { currentOnDragStart() },
                                onDragEnd = { currentOnDragEnd() },
                                onDragCancel = { currentOnDragEnd() },
                                onDrag = { change, drag ->
                                    change.consume()
                                    currentOnDrag(drag.y)
                                },
                            )
                        },
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = Icons.Default.DragHandle, contentDescription = null)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListState.rowHeightFor(
    tab: BottomNavBloc.Tab
): Float? =
    layoutInfo.visibleItemsInfo.firstOrNull { (it.key as? String) == tab.name }?.size?.toFloat()
