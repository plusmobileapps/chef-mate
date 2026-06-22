@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A bottom-anchored horizontal floating toolbar that automatically rides up above a shown snackbar,
 * so it is never covered. Wraps Material's [HorizontalFloatingToolbar] and bakes in the
 * [LocalSnackbarInset] bottom padding — the toolbar equivalent of [PlusFloatingActionButton].
 * Prefer this over a raw `HorizontalFloatingToolbar` for a screen's bottom action toolbar.
 *
 * @param expanded whether the toolbar is expanded; forwarded to [HorizontalFloatingToolbar].
 * @param floatingActionButton optional FAB shown alongside the toolbar (rides up with it).
 * @param content the toolbar's action items, laid out in a `Row`.
 */
@Composable
fun PlusToolbar(
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    // Lift the toolbar above any shown snackbar.
    val toolbarModifier = modifier.padding(bottom = LocalSnackbarInset.current)
    // HorizontalFloatingToolbar exposes the FAB via a separate overload (non-null param), so route
    // to whichever matches.
    if (floatingActionButton != null) {
        HorizontalFloatingToolbar(
            expanded = expanded,
            floatingActionButton = floatingActionButton,
            modifier = toolbarModifier,
            content = content,
        )
    } else {
        HorizontalFloatingToolbar(
            expanded = expanded,
            modifier = toolbarModifier,
            content = content,
        )
    }
}
