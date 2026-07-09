package com.plusmobileapps.chefmate.update

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/** Desktop-only self-update banner. Renders nothing when [state] is [UpdateState.Idle]. */
@Composable
fun UpdateBanner(
    state: UpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state is UpdateState.Idle) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChefMateTheme.colorScheme.primaryContainer,
        contentColor = ChefMateTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 3.dp,
    ) {
        Column(modifier = Modifier.padding(ChefMateTheme.dimens.paddingNormal)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(ChefMateTheme.dimens.paddingSmall),
            ) {
                Text(
                    text = bannerText(state),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ChefMateTheme.typography.bodyMedium,
                )
                when (state) {
                    is UpdateState.Available -> {
                        TextButton(onClick = onDownload) { Text("Download") }
                    }
                    is UpdateState.Ready -> {
                        TextButton(onClick = onInstall) { Text("Restart & install") }
                    }
                    else -> Unit
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss")
                }
            }
            if (state is UpdateState.Downloading) {
                if (state.fraction >= 0f) {
                    LinearProgressIndicator(
                        progress = { state.fraction },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        }
    }
}

private fun bannerText(state: UpdateState): String =
    when (state) {
        is UpdateState.Available -> "Version ${state.version} is available"
        is UpdateState.Downloading -> "Downloading update…"
        is UpdateState.Ready -> "Update ready to install"
        UpdateState.Idle -> ""
    }
