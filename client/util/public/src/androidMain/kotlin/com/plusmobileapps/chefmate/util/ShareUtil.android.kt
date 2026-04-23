@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareLauncher(): (text: String) -> Boolean {
    val context = LocalContext.current
    return remember(context) {
        { text ->
            val sendIntent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            context.startActivity(Intent.createChooser(sendIntent, null))
            false
        }
    }
}
