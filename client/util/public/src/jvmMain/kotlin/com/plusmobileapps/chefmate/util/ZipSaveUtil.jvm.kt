@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.SwingUtilities

@Composable
actual fun rememberZipSaveLauncher(
    onResult: (Boolean) -> Unit
): (fileName: String, bytes: ByteArray) -> Unit {
    val currentOnResult = rememberUpdatedState(onResult)
    return remember {
        { fileName, bytes ->
            SwingUtilities.invokeLater {
                val dialog =
                    FileDialog(null as Frame?, "Save recipe archive", FileDialog.SAVE).apply {
                        file = fileName
                        isVisible = true
                    }
                val directory = dialog.directory
                val name = dialog.file
                if (directory == null || name == null) {
                    currentOnResult.value(false)
                    return@invokeLater
                }
                val chosen = File(directory, name)
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
