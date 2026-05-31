@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.plusmobileapps.chefmate.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Secondary [AnimatedVisibilityScope] for elements that participate in *two* transitions
 * simultaneously. Compose's shared-element framework only morphs when both source and destination
 * AVSes are transitioning, so an element bridging an outer (cross-screen) and inner (in-screen
 * overlay) transition needs a second registration with this scope and a distinct key. See
 * `RecipeDetailScreen` for the reference wiring.
 */
val LocalSecondaryAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Applies `Modifier.sharedElement` when [key] is non-null and both scopes are present. Use for
 * visually identical content morphing between locations (e.g. an image).
 */
@Composable
fun Modifier.sharedElementBy(key: String?): Modifier =
    sharedElementBy(key = key, animatedVisibilityScope = LocalAnimatedVisibilityScope.current)

/**
 * Variant of [sharedElementBy] that takes an explicit [animatedVisibilityScope] instead of pulling
 * it from [LocalAnimatedVisibilityScope]. Use this when registering a *secondary* shared element on
 * an element already bound to a different AVS — see [LocalSecondaryAnimatedVisibilityScope].
 */
@Composable
fun Modifier.sharedElementBy(
    key: String?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    return if (key != null && sharedScope != null && animatedVisibilityScope != null) {
        with(sharedScope) {
            this@sharedElementBy.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope,
            )
        }
    } else this
}
