package com.plusmobileapps.chefmate.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.ui.components.PlusSnackbarHost

/**
 * The single host that displays toasts enqueued through [ToastService]. Place it once near the app
 * root, above the navigation stack, so messages float over whatever screen is showing. Drains the
 * service's queue through [PlusSnackbarHost], so messages play in sequence with none dropped.
 */
@Composable
fun ToastServiceHost(service: ToastService, modifier: Modifier = Modifier) {
    val queue by service.queue.collectAsState()
    PlusSnackbarHost(queue = queue, onSnackbarShown = service::onShown, modifier = modifier)
}
