@file:OptIn(ExperimentalMaterial3Api::class)

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chefmate.client.ui.public.generated.resources.Res
import chefmate.client.ui.public.generated.resources.close
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Reusable modal bottom sheet chrome: drag handle, optional title row, optional close icon. Pair
 * with a child-slot dismiss-animation pattern at the call site when the slot's lifetime is owned by
 * a Bloc — this component only renders visuals.
 */
@Composable
fun PlusSheetModal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: TextData? = null,
    onCloseClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        if (title != null || onCloseClick != null) {
            PlusSheetModalHeader(title = title, onCloseClick = onCloseClick)
        }
        content()
    }
}

/**
 * Visual chrome for the top of a [PlusSheetModal] — exposed publicly so screenshot tests and
 * non-modal sheet variants can render it directly, since [ModalBottomSheet] itself doesn't snapshot
 * reliably under the Compose screenshot plugin.
 */
@Composable
fun PlusSheetModalHeader(title: TextData?, onCloseClick: (() -> Unit)?) {
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
