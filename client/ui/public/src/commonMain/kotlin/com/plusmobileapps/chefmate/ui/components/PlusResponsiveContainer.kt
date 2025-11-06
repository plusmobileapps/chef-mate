package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class WindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

@Composable
fun PlusResponsiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable (WindowSizeClass) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier,
    ) {
        val windowSizeClass =
            when {
                maxWidth < 600.dp -> WindowSizeClass.COMPACT
                maxWidth < 840.dp -> WindowSizeClass.MEDIUM
                else -> WindowSizeClass.EXPANDED
            }
        content(windowSizeClass)
    }
}