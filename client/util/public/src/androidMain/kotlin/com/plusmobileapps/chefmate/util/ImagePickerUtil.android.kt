@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult = rememberUpdatedState(onResult)
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri == null) {
                currentOnResult.value(null)
                return@rememberLauncherForActivityResult
            }
            val resolver = context.contentResolver
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                currentOnResult.value(null)
                return@rememberLauncherForActivityResult
            }
            val mimeType = resolver.getType(uri)
            val extension =
                when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    "image/heic" -> "heic"
                    "image/gif" -> "gif"
                    else -> "jpg"
                }
            currentOnResult.value(PickedImage(bytes = bytes, fileExtension = extension))
        }
    return remember(launcher) {
        {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}
