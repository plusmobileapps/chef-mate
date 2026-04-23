@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@Composable
actual fun rememberShareLauncher(): (text: String) -> Boolean = { text ->
    val rootVC =
        UIApplication.sharedApplication.windows
            .firstOrNull { (it as? platform.UIKit.UIWindow)?.isKeyWindow() == true }
            ?.let { (it as platform.UIKit.UIWindow).rootViewController }
    val activityVC =
        UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    rootVC?.presentViewController(activityVC, animated = true, completion = null)
    false
}
