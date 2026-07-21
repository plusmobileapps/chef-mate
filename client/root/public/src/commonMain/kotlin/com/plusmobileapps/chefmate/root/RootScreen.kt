@file:OptIn(FaultyDecomposeApi::class, ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import com.arkivanov.decompose.FaultyDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimator
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.aichat.AiChatPresentation
import com.plusmobileapps.chefmate.aichat.LocalAiChatPresentation
import com.plusmobileapps.chefmate.aichat.LocalAiChatRequestExpand
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun RootScreen(rootBloc: RootBloc, modifier: Modifier = Modifier) {
    val stack by rootBloc.state.subscribeAsState()
    ChefMateTheme {
        // Surface paints the theme background behind the navigation stack so the
        // Android predictive-back gesture reveals the themed color (white in light,
        // black in dark) instead of the Activity's hardcoded light window background.
        Surface(modifier = modifier.fillMaxSize()) {
            Children(
                modifier = Modifier.fillMaxSize(),
                stack = stack,
                animation =
                    backAnimation(
                        backHandler = rootBloc.backHandler,
                        onBack = rootBloc::onBackClicked,
                        fallbackAnimation =
                            stackAnimation { child, otherChild, _ ->
                                if (child.instance.isModal() || otherChild.instance.isModal()) {
                                    verticalSlide()
                                } else {
                                    slide()
                                }
                            },
                        isModal = { it.isModal() },
                    ),
            ) { child ->
                child.instance.bloc.Content()
            }
        }
        // Recipe-grounded AI chat, layered over the current screen as an expandable bottom sheet.
        AiChatSheet(rootBloc)
    }
}

/**
 * The recipe-grounded AI chat sheet. It opens as a small "peek" (just the input over the dimmed
 * recipe) and grows to full screen once [AiChatPresentation.SheetExpanded] is requested — by
 * focusing the input, dragging the peek strip up, or sending a message.
 */
@Composable
private fun AiChatSheet(rootBloc: RootBloc) {
    val slot by rootBloc.aiChatSheetSlot.subscribeAsState()
    val child = slot.child?.instance

    // Keep the last child in composition through the hide animation after the slot dismisses.
    var sheetChild by remember { mutableStateOf(child) }
    if (child != null) sheetChild = child

    // Peek vs full is host state (not a Material detent) so the peek can hug just the input. Keyed
    // on the child so each newly-opened chat starts collapsed, while staying stable (and expanded)
    // through the dismiss animation.
    var expanded by remember(sheetChild) { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(child) {
        if (child == null && sheetChild != null) {
            sheetState.hide()
            sheetChild = null
        }
    }

    val currentChild = sheetChild ?: return
    ModalBottomSheet(
        onDismissRequest = rootBloc::onAiChatSheetDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        // When expanded the chat's own header is the top of the sheet; the drag handle only shows
        // in
        // the peek, where it invites the drag-to-expand gesture.
        dragHandle = if (expanded) null else ({ BottomSheetDefaults.DragHandle() }),
    ) {
        CompositionLocalProvider(
            LocalAiChatPresentation provides
                if (expanded) AiChatPresentation.SheetExpanded else AiChatPresentation.SheetPeek,
            LocalAiChatRequestExpand provides { expanded = true },
        ) {
            // Grow (and shrink) the sheet smoothly between the compact peek and the full-height
            // chat instead of snapping. Anchoring the size change to the bottom keeps the input
            // pinned to the bottom edge while the app bar and messages fill in above as it expands.
            Box(
                modifier =
                    Modifier.fillMaxWidth()
                        .animateContentSize(
                            animationSpec = tween(),
                            alignment = Alignment.BottomCenter,
                        )
            ) {
                currentChild.bloc.Content()
            }
        }
    }
}

// Screens that slide up/down over the stack rather than horizontally.
private fun RootBloc.Child.isModal(): Boolean =
    this is RootBloc.Child.Browser ||
        this is RootBloc.Child.MealPlanner ||
        this is RootBloc.Child.CookMode ||
        this is RootBloc.Child.EditRecipeBook ||
        this is RootBloc.Child.EditGroceryList

private fun verticalSlide(): StackAnimator = stackAnimator { factor, direction, content ->
    content(Modifier.offsetYFactor(if (direction.isFront) factor else 0f))
}

private fun Modifier.offsetYFactor(factor: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(x = 0, y = (placeable.height.toFloat() * factor).toInt())
    }
}
