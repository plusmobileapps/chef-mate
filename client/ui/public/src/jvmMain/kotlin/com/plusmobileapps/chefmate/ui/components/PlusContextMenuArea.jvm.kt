@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable

@Composable
actual fun PlusContextMenuArea(
    items: () -> List<PlusContextMenuItem>,
    content: @Composable () -> Unit,
) {
    ContextMenuArea(
        items = { items().map { ContextMenuItem(it.label, it.onClick) } },
        content = content,
    )
}
