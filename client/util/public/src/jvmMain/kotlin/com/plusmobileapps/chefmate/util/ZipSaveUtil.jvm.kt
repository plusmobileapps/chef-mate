@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberZipSaveLauncher(
    onResult: (Boolean) -> Unit
): (fileName: String, bytes: ByteArray) -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        { fileName, bytes ->
            SwingUtilities.invokeLater {
                val chooser =
                    JFileChooser().apply {
                        dialogTitle = "Save recipe archive"
                        fileFilter = FileNameExtensionFilter("Zip archives", "zip")
                        selectedFile = File(fileName)
                    }
                if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) {
                    currentOnResult.value(false)
                    return@invokeLater
                }
                val chosen = chooser.selectedFile
                val target =
                    if (chosen.name.endsWith(".zip", ignoreCase = true)) chosen
                    else File(chosen.parentFile, chosen.name + ".zip")
                val saved =
                    try {
                        target.writeBytes(bytes)
                        true
                    } catch (t: Throwable) {
                        false
                    }
                currentOnResult.value(saved)
            }
        }
    }
}
