@file:OptIn(FaultyDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import com.plusmobileapps.chefmate.subscription.PremiumRequiredDialog
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun RootScreen(rootBloc: RootBloc, modifier: Modifier = Modifier) {
    val stack by rootBloc.state.subscribeAsState()
    val showPremiumDialog by rootBloc.premiumDialog.subscribeAsState()
    ChefMateTheme {
        if (showPremiumDialog) {
            PremiumRequiredDialog(
                onConfirm = rootBloc::onPremiumDialogConfirmed,
                onDismiss = rootBloc::onPremiumDialogDismissed,
            )
        }
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
                val instance = child.instance
                if (instance is RootBloc.Child.AiChat) {
                    // The AI chat is a full-screen modal: its app bar carries a close (X) plus
                    // history and new-conversation actions rather than a back arrow.
                    CompositionLocalProvider(
                        LocalAiChatPresentation provides AiChatPresentation.SheetExpanded
                    ) {
                        instance.bloc.Content()
                    }
                } else {
                    instance.bloc.Content()
                }
            }
        }
    }
}

// Screens that slide up/down over the stack rather than horizontally.
private fun RootBloc.Child.isModal(): Boolean =
    this is RootBloc.Child.Browser ||
        this is RootBloc.Child.MealPlanner ||
        this is RootBloc.Child.CookMode ||
        this is RootBloc.Child.AiChat ||
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
