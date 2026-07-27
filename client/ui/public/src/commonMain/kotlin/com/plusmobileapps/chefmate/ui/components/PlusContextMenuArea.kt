package com.plusmobileapps.chefmate.ui.components

import androidx.compose.runtime.Composable

/** A single entry in a [PlusContextMenuArea] menu. */
data class PlusContextMenuItem(val label: String, val onClick: () -> Unit)

/**
 * Wraps [content] in a right-click (secondary-click) context menu.
 *
 * Only desktop has a mouse to right-click with, so the JVM actual is the only one that renders a
 * menu — Android and iOS emit [content] untouched. Gate the call site on something meaningful (a
 * desktop-only composition local, say) rather than leaning on the no-op actuals, so touch platforms
 * never build items they could not show.
 */
@Composable
expect fun PlusContextMenuArea(
    items: () -> List<PlusContextMenuItem>,
    content: @Composable () -> Unit,
)
