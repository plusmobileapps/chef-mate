package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable

data class PickedImage(val bytes: ByteArray, val fileExtension: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedImage) return false
        if (fileExtension != other.fileExtension) return false
        if (!bytes.contentEquals(other.bytes)) return false
        return true
    }

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileExtension.hashCode()
}

/**
 * Returns a launcher that opens the platform's image picker. The provided callback receives the
 * picked image bytes and file extension, or `null` if the user cancelled.
 */
@Composable expect fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit
