@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberZipSaveLauncher(
    onResult: (Boolean) -> Unit
): (fileName: String, bytes: ByteArray) -> Unit {
    val context = LocalContext.current
    val currentOnResult = rememberUpdatedState(onResult)
    // The CreateDocument contract only returns a Uri, so the bytes have to be stashed at launch
    // time and read again from the activity-result callback.
    val pendingBytes = remember { mutableStateOf<ByteArray?>(null) }
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip")
        ) { uri ->
            val bytes = pendingBytes.value
            pendingBytes.value = null
            if (uri == null || bytes == null) {
                currentOnResult.value(false)
                return@rememberLauncherForActivityResult
            }
            val saved =
                try {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(bytes)
                        true
                    } ?: false
                } catch (t: Throwable) {
                    false
                }
            currentOnResult.value(saved)
        }
    return remember(launcher) {
        { fileName, bytes ->
            pendingBytes.value = bytes
            launcher.launch(fileName)
        }
    }
}
