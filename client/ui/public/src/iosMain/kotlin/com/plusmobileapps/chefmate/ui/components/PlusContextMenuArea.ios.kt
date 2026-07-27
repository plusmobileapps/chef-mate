@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.runtime.Composable

/** No mouse, no right-click — the menu is desktop-only, so the content passes straight through. */
@Composable
actual fun PlusContextMenuArea(
    items: () -> List<PlusContextMenuItem>,
    content: @Composable () -> Unit,
) {
    content()
}
