@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePickerLauncher(onResult: (PickedImage?) -> Unit): () -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        {
            SwingUtilities.invokeLater {
                val chooser =
                    JFileChooser().apply {
                        dialogTitle = "Select recipe photo"
                        fileFilter =
                            FileNameExtensionFilter(
                                "Images",
                                "jpg",
                                "jpeg",
                                "png",
                                "webp",
                                "gif",
                                "heic",
                            )
                    }
                val approved = chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
                if (!approved) {
                    currentOnResult.value(null)
                    return@invokeLater
                }
                val file = chooser.selectedFile
                if (file == null || !file.canRead()) {
                    currentOnResult.value(null)
                    return@invokeLater
                }
                val extension = file.extension.ifBlank { "jpg" }.lowercase()
                currentOnResult.value(
                    PickedImage(bytes = file.readBytes(), fileExtension = extension)
                )
            }
        }
    }
}
