@file:OptIn(ExperimentalDecomposeApi::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.StackAnimator
import com.arkivanov.decompose.extensions.compose.stack.animation.isFront
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimator
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.auth.ui.AuthenticationScreen
import com.plusmobileapps.chefmate.browser.BrowserScreen
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavigationScreen
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootScreen
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun RootScreen(rootBloc: RootBloc, modifier: Modifier = Modifier) {
    val state = rootBloc.state.subscribeAsState()
    ChefMateTheme {
        Children(
            modifier = modifier.fillMaxSize(),
            stack = state.value,
            animation =
                predictiveBackAnimation(
                    backHandler = rootBloc.backHandler,
                    fallbackAnimation =
                        stackAnimation { child, otherChild, _ ->
                            if (
                                child.instance is RootBloc.Child.Browser ||
                                    otherChild.instance is RootBloc.Child.Browser
                            ) {
                                verticalSlide()
                            } else {
                                slide()
                            }
                        },
                    onBack = rootBloc::onBackClicked,
                ),
            content = {
                when (val child = it.instance) {
                    is RootBloc.Child.BottomNavigation -> BottomNavigationScreen(child.bloc)
                    is RootBloc.Child.GroceryDetail -> GroceryDetailScreen(child.bloc)
                    is RootBloc.Child.RecipeRoot -> RecipeRootScreen(child.bloc)
                    is RootBloc.Child.Authentication -> AuthenticationScreen(child.bloc)
                    is RootBloc.Child.Browser ->
                        PlusHeaderContainer(
                            modifier = Modifier.fillMaxSize(),
                            data =
                                PlusHeaderData.Modal(
                                    title = FixedString(""),
                                    onCloseClick = rootBloc::onBackClicked,
                                ),
                            scrollEnabled = false,
                            maxContentWidth = Dp.Unspecified,
                            content = { BrowserScreen(child.bloc, modifier = Modifier.weight(1f)) },
                        )
                }
            },
        )
    }
}

private fun verticalSlide(): StackAnimator = stackAnimator { factor, direction, content ->
    content(Modifier.offsetYFactor(if (direction.isFront) factor else 0f))
}

private fun Modifier.offsetYFactor(factor: Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.placeRelative(x = 0, y = (placeable.height.toFloat() * factor).toInt())
    }
}
