@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
actual fun rememberShareLauncher(): (text: String) -> Unit {
    val clipboardManager = LocalClipboardManager.current
    return remember(clipboardManager) {
        { text -> clipboardManager.setText(AnnotatedString(text)) }
    }
}
