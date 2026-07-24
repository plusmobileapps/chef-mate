@file:OptIn(FaultyDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        // Recipe-grounded AI chat, layered full-screen over the current screen.
        AiChatModal(rootBloc)
    }
}

/**
 * The recipe-grounded AI chat, opened from a recipe or Cook Mode. It covers the screen as a
 * full-screen modal that slides up from the bottom, and is dismissed by its close button or the
 * system back gesture (handled by the host slot's back callback).
 */
@Composable
private fun AiChatModal(rootBloc: RootBloc) {
    val slot by rootBloc.aiChatSheetSlot.subscribeAsState()
    val child = slot.child?.instance

    // Keep the last child composed through the slide-out animation after the slot dismisses.
    var lastChild by remember { mutableStateOf(child) }
    if (child != null) lastChild = child

    AnimatedVisibility(
        visible = child != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
    ) {
        lastChild?.let { current ->
            CompositionLocalProvider(
                LocalAiChatPresentation provides AiChatPresentation.SheetExpanded
            ) {
                Surface(modifier = Modifier.fillMaxSize()) { current.bloc.Content() }
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
