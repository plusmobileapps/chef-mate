package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable

data class PickedFile(val bytes: ByteArray, val fileName: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedFile) return false
        if (fileName != other.fileName) return false
        if (!bytes.contentEquals(other.bytes)) return false
        return true
    }

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileName.hashCode()
}

/**
 * Returns a launcher that opens the platform's document picker filtered to `.zip` archives. The
 * provided callback receives the picked file's raw bytes and name, or `null` if the user cancelled.
 */
@Composable expect fun rememberZipPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit
