@file:OptIn(ExperimentalDecomposeApi::class, ExperimentalSharedTransitionApi::class)

package com.plusmobileapps.chefmate.root

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
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
import com.plusmobileapps.chefmate.auth.ui.otp.OtpScreen
import com.plusmobileapps.chefmate.browser.BrowserRootScreen
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsScreen
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsScreen
import com.plusmobileapps.chefmate.grocery.core.detail.GroceryDetailScreen
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavigationScreen
import com.plusmobileapps.chefmate.recipe.core.addmeal.MealPlannerRootScreen
import com.plusmobileapps.chefmate.recipe.core.root.RecipeRootScreen
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
                // contentKey doubles as the key for AnimatedContent's internal
                // SaveableStateHolder, which Bundle-validates the key on Android. The default
                // Decompose Child.key is the Configuration data object, which is not
                // Bundle-serializable — pass our derived String instead.
                contentKey = { it.saveableKey() },
                transitionSpec = {
                    val current = targetState.instance
                    val previous = initialState.instance
                    val isVertical =
                        current is RootBloc.Child.Browser ||
                            previous is RootBloc.Child.Browser ||
                            current is RootBloc.Child.MealPlanner ||
                            previous is RootBloc.Child.MealPlanner ||
                            current is RootBloc.Child.CookMode ||
                            previous is RootBloc.Child.CookMode
                    val items = stack.items
                    val initialIndex = items.indexOfFirst { it.key == initialState.key }
                    val targetIndex = items.indexOfFirst { it.key == targetState.key }
                    val isForward = if (initialIndex < 0) false else targetIndex > initialIndex
                    val spec = tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 300)
                    val floatSpec = tween<Float>(durationMillis = 300)

                    if (isVertical) {
                        if (isForward) {
                            // Modal slides up over the background. The background must stay alive
                            // (via fadeOut) for the full duration so it's visible behind the
                            // incoming screen; z-index puts the modal on top.
                            ContentTransform(
                                targetContentEnter = slideInVertically(spec) { it },
                                initialContentExit = fadeOut(floatSpec),
                                targetContentZIndex = 1f,
                            )
                        } else {
                            // Modal slides back down. The background should sit underneath while
                            // the modal exits; negative z-index keeps the modal on top.
                            ContentTransform(
                                targetContentEnter = EnterTransition.None,
                                initialContentExit = slideOutVertically(spec) { it },
                                targetContentZIndex = -1f,
                            )
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
                // Only BottomNavigation and RecipeRoot participate in shared element transitions
                // (recipe image/title morph). All other screens get a null AnimatedVisibilityScope
                // so SharedTransitionLayout never intercepts their enter/exit timing.
                val isSharedTransitionParticipant =
                    activeChild.instance is RootBloc.Child.BottomNavigation ||
                        activeChild.instance is RootBloc.Child.RecipeRoot
                CompositionLocalProvider(
                    LocalSharedTransitionScope provides this@SharedTransitionLayout,
                    LocalAnimatedVisibilityScope provides
                        if (isSharedTransitionParticipant) this else null,
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
        is RootBloc.Child.OtpVerification -> OtpScreen(child.bloc)
        is RootBloc.Child.Browser ->
            PlusHeaderContainer(
                modifier = Modifier.fillMaxSize(),
                data = PlusHeaderData.Modal(title = FixedString(""), onCloseClick = onBack),
                scrollEnabled = false,
                maxContentWidth = Dp.Unspecified,
                content = { BrowserRootScreen(child.bloc, modifier = Modifier.weight(1f)) },
            )
        is RootBloc.Child.MealPlanner -> MealPlannerRootScreen(child.bloc)
        is RootBloc.Child.AppSettings -> child.Content()
        is RootBloc.Child.BottomNavOrder -> child.Content()
        is RootBloc.Child.DeveloperSettings -> DeveloperSettingsScreen(child.bloc)
        is RootBloc.Child.FeatureFlags -> FeatureFlagsScreen(child.bloc)
        is RootBloc.Child.CookMode -> child.Content()
    }
}
