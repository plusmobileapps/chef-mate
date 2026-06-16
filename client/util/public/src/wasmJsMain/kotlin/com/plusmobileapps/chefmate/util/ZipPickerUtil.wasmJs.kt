@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberZipPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    // Stubbed for the web spike. A real implementation would use a hidden <input type="file">.
    val currentOnResult = rememberUpdatedState(onResult)
    return remember { { currentOnResult.value(null) } }
}
