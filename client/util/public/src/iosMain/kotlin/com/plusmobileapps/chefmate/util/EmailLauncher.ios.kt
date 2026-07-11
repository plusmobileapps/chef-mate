@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberEmailLauncher(): (email: String) -> Unit = { email ->
    val url = NSURL(string = "mailto:$email")
    UIApplication.sharedApplication.openURL(
        url,
        options = mapOf<Any?, Any>(),
        completionHandler = null,
    )
}
