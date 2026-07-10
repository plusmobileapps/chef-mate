@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberEmailLauncher(): (email: String) -> Unit {
    val context = LocalContext.current
    return remember(context) {
        { email ->
            val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$email") }
            // ACTION_SENDTO with a mailto: URI resolves only to email apps. Guard against devices
            // with no mail client so the tap can't crash the app.
            try {
                context.startActivity(intent)
            } catch (_: ActivityNotFoundException) {
                // No email client installed — nothing to do.
            }
        }
    }
}
