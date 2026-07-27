@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * macOS shares the Apple share glyph with iOS; Windows and Linux have no system equivalent, so they
 * keep the Material icon.
 */
actual val PlusShareIcon: ImageVector
    get() = if (isMacOs) Icons.Default.IosShare else Icons.Default.Share

private val isMacOs: Boolean by lazy {
    val os = System.getProperty("os.name").orEmpty().lowercase()
    os.contains("mac") || os.contains("darwin")
}
