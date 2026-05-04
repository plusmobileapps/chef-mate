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
 * Applies `Modifier.sharedElement` when [key] is non-null and both scopes are present. Use for
 * visually identical content morphing between locations (e.g. an image).
 */
@Composable
fun Modifier.sharedElementBy(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current
    return if (key != null && sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            this@sharedElementBy.sharedElement(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedScope,
            )
        }
    } else this
}

/**
 * Applies `Modifier.sharedBounds` when [key] is non-null and both scopes are present. Use when the
 * source and target render differently (e.g. text at different styles) — bounds morph while content
 * crossfades.
 */
@Composable
fun Modifier.sharedBoundsBy(key: String?): Modifier {
    val sharedScope = LocalSharedTransitionScope.current
    val animatedScope = LocalAnimatedVisibilityScope.current
    return if (key != null && sharedScope != null && animatedScope != null) {
        with(sharedScope) {
            this@sharedBoundsBy.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedScope,
            )
        }
    } else this
}
