@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState

@Composable
actual fun rememberZipSaveLauncher(
    onResult: (Boolean) -> Unit
): (fileName: String, bytes: ByteArray) -> Unit {
    // Stubbed for the web spike. A real implementation would trigger a Blob download anchor.
    val currentOnResult = rememberUpdatedState(onResult)
    return remember { { _, _ -> currentOnResult.value(false) } }
}
