@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import javax.swing.SwingUtilities

@Composable
actual fun rememberZipPickerLauncher(onResult: (PickedFile?) -> Unit): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        {
            SwingUtilities.invokeLater {
                val dialog =
                    FileDialog(null as Frame?, "Select recipe archive", FileDialog.LOAD).apply {
                        filenameFilter = FilenameFilter { _, name ->
                            name.endsWith(".zip", ignoreCase = true)
                        }
                        isVisible = true
                    }
                val directory = dialog.directory
                val name = dialog.file
                if (directory == null || name == null) {
                    currentOnResult.value(null)
                    return@invokeLater
                }
                val file = File(directory, name)
                if (!file.canRead()) {
                    currentOnResult.value(null)
                    return@invokeLater
                }
                currentOnResult.value(PickedFile(bytes = file.readBytes(), fileName = file.name))
            }
        }
    }
}
