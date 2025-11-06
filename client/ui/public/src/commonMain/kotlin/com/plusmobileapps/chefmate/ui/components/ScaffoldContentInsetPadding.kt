package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Stable
@Composable
fun Modifier.scaffoldContentInsetPadding(): Modifier {
    val windowInsets = WindowInsets.displayCutout
    val density = LocalDensity.current

    return with(density) {
        padding(
            paddingValues =
                PaddingValues.Absolute(
                    left = windowInsets.getLeft(density, LayoutDirection.Ltr).toDp(),
                    right = windowInsets.getRight(density, LayoutDirection.Ltr).toDp(),
                ),
        )
    }
}

/**
 * To be used in a [Scaffold] content window insets that is a child in bottom navigation.
 */
@Composable
fun ScaffoldContentWindowInsets(): WindowInsets {
    val density = LocalDensity.current
    return WindowInsets(
        top =
            with(density) {
                WindowInsets.statusBars.getTop(density).toDp()
            },
        bottom = 0.dp,
        right =
            with(density) {
                WindowInsets.displayCutout.getRight(density, LayoutDirection.Ltr).toDp()
            },
    )
}