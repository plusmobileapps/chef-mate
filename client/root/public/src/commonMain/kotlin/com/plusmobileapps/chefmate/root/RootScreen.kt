@file:OptIn(FaultyDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun RootScreen(rootBloc: RootBloc, modifier: Modifier = Modifier) {
    val stack by rootBloc.state.subscribeAsState()
    ChefMateTheme {
        Children(
            modifier = modifier.fillMaxSize(),
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
                ),
        ) { child ->
            child.instance.Content()
        }
    }
}

// Screens that slide up/down over the stack rather than horizontally.
private fun RootBloc.Child.isModal(): Boolean =
    this is RootBloc.Child.Browser ||
        this is RootBloc.Child.MealPlanner ||
        this is RootBloc.Child.CookMode

private fun verticalSlide(): StackAnimator = stackAnimator { factor, direction, content ->
    content(Modifier.offsetYFactor(if (direction.isFront) factor else 0f))
}

private fun Modifier.offsetYFactor(factor: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(x = 0, y = (placeable.height.toFloat() * factor).toInt())
    }
}
