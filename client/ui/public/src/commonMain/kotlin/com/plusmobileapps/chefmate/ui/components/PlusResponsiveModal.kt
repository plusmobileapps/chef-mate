@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.close
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Modal that adapts to the available window width: on [WindowSizeClass.EXPANDED] it renders as a
 * centered floating dialog; on smaller widths it renders as a [ModalBottomSheet]. The header
 * (optional title + close icon) is shared between both presentations.
 *
 * The provided [sheetState] is only meaningful for the sheet branch; it is ignored in the dialog
 * branch where the platform [Dialog] manages its own animations.
 */
@Composable
fun PlusResponsiveModal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: TextData? = null,
    onCloseClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlusResponsiveContainer {
        if (it == WindowSizeClass.EXPANDED) {
            PlusResponsiveModalDialog(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                title = title,
                onCloseClick = onCloseClick,
                content = content,
            )
        } else {
            PlusResponsiveModalSheet(
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                sheetState = sheetState,
                title = title,
                onCloseClick = onCloseClick,
                content = content,
            )
        }
    }
}

@Composable
private fun PlusResponsiveModalSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    sheetState: SheetState,
    title: TextData?,
    onCloseClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        if (title != null || onCloseClick != null) {
            PlusResponsiveModalHeader(title = title, onCloseClick = onCloseClick)
        }
        content()
    }
}

@Composable
private fun PlusResponsiveModalDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: TextData?,
    onCloseClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        PlusResponsiveModalDialogSurface(
            modifier = modifier,
            title = title,
            onCloseClick = onCloseClick,
            content = content,
        )
    }
}

/**
 * Visual surface used inside [PlusResponsiveModal]'s expanded-width branch — exposed publicly so
 * screenshot tests can render the dialog presentation without going through [Dialog], which doesn't
 * snapshot reliably under the Compose screenshot plugin.
 */
@Composable
fun PlusResponsiveModalDialogSurface(
    title: TextData?,
    onCloseClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = ChefMateTheme.dimens
    Surface(
        modifier = modifier.widthIn(max = 560.dp).padding(dimens.paddingNormal),
        shape = ChefMateTheme.shapes.large,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.padding(top = dimens.paddingNormal)) {
            if (title != null || onCloseClick != null) {
                PlusResponsiveModalHeader(title = title, onCloseClick = onCloseClick)
            }
            content()
        }
    }
}

/**
 * Visual chrome for the top of a [PlusResponsiveModal] — exposed publicly so screenshot tests and
 * non-modal callers can render it directly, since [ModalBottomSheet] itself doesn't snapshot
 * reliably under the Compose screenshot plugin.
 */
@Composable
fun PlusResponsiveModalHeader(title: TextData?, onCloseClick: (() -> Unit)?) {
    val dimens = ChefMateTheme.dimens
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(
                    start = dimens.paddingNormal,
                    end = dimens.paddingSmall,
                    bottom = dimens.paddingSmall,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(text = title.localized(), style = MaterialTheme.typography.titleLarge)
            }
        }
        if (onCloseClick != null) {
            PlusIconButton(
                icon = Icons.Default.Close,
                contentDescription = stringResource(Res.string.close),
                onClick = onCloseClick,
                size = 40.dp,
            )
        }
    }
}
