@file:OptIn(ExperimentalDecomposeApi::class, ExperimentalSharedTransitionApi::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.auth.ui.AuthenticationScreen
import com.plusmobileapps.chefmate.browser.BrowserRootScreen
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavigationScreen
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootScreen
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootScreen
import com.plusmobileapps.chefmate.settings.AppSettingsScreen
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.LocalAnimatedVisibilityScope
import com.plusmobileapps.chefmate.ui.LocalSharedTransitionScope
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun RootScreen(rootBloc: RootBloc, modifier: Modifier = Modifier) {
    val stack by rootBloc.state.subscribeAsState()
    val saveableStateHolder = rememberSaveableStateHolder()
    val previousKeys = remember { mutableSetOf<String>() }

    DisposableEffect(stack) {
        val currentKeys = stack.items.mapTo(HashSet()) { it.saveableKey() }
        previousKeys.forEach { key ->
            if (key !in currentKeys) saveableStateHolder.removeState(key)
        }
        previousKeys.clear()
        previousKeys.addAll(currentKeys)
        onDispose {}
    }

    ChefMateTheme {
        SharedTransitionLayout(modifier = modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = stack.active,
                contentKey = { it.key },
                transitionSpec = {
                    val current = targetState.instance
                    val previous = initialState.instance
                    val isVertical =
                        current is RootBloc.Child.Browser ||
                            previous is RootBloc.Child.Browser ||
                            current is RootBloc.Child.MealPlanner ||
                            previous is RootBloc.Child.MealPlanner
                    val items = stack.items
                    val initialIndex = items.indexOfFirst { it.key == initialState.key }
                    val targetIndex = items.indexOfFirst { it.key == targetState.key }
                    val isForward = if (initialIndex < 0) false else targetIndex > initialIndex
                    val spec = tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 300)

                    if (isVertical) {
                        if (isForward) {
                            slideInVertically(spec) { it } togetherWith
                                slideOutVertically(spec) { 0 }
                        } else {
                            slideInVertically(spec) { 0 } togetherWith
                                slideOutVertically(spec) { it }
                        }
                    } else {
                        if (isForward) {
                            slideInHorizontally(spec) { it } togetherWith
                                slideOutHorizontally(spec) { -it / 4 }
                        } else {
                            slideInHorizontally(spec) { -it / 4 } togetherWith
                                slideOutHorizontally(spec) { it }
                        }
                    }
                },
                label = "root-stack",
            ) { activeChild ->
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides this,
                ) {
                    saveableStateHolder.SaveableStateProvider(activeChild.saveableKey()) {
                        RootChildContent(activeChild.instance, rootBloc::onBackClicked)
                    }
                }
            }
        }
    }
}

private fun com.arkivanov.decompose.Child<*, *>.saveableKey(): String =
    "${configuration::class.simpleName}_${key.hashCode()}"

@Composable
private fun RootChildContent(child: RootBloc.Child, onBack: () -> Unit) {
    when (child) {
        is RootBloc.Child.BottomNavigation -> BottomNavigationScreen(child.bloc)
        is RootBloc.Child.GroceryDetail -> GroceryDetailScreen(child.bloc)
        is RootBloc.Child.RecipeRoot -> RecipeRootScreen(child.bloc)
        is RootBloc.Child.Authentication -> AuthenticationScreen(child.bloc)
        is RootBloc.Child.Browser ->
            PlusHeaderContainer(
                modifier = Modifier.fillMaxSize(),
                data = PlusHeaderData.Modal(title = FixedString(""), onCloseClick = onBack),
                scrollEnabled = false,
                maxContentWidth = Dp.Unspecified,
                content = { BrowserRootScreen(child.bloc, modifier = Modifier.weight(1f)) },
            )
        is RootBloc.Child.MealPlanner -> MealPlannerRootScreen(child.bloc)
        is RootBloc.Child.AppSettings -> AppSettingsScreen(child.bloc)
    }
}
