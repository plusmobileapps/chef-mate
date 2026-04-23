@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.darwin.DISPATCH_TIME_NOW
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

@Composable
actual fun rememberShareLauncher(): (text: String) -> Boolean = { text ->
    // Delay presentation so the Compose dropdown popup fully tears down first.
    val delayNs = (0.3 * 1_000_000_000).toLong() // 300 ms
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, delayNs), dispatch_get_main_queue()) {
        val keyWindow =
            UIApplication.sharedApplication.connectedScenes
                .filterIsInstance<UIWindowScene>()
                .flatMap { it.windows.map { w -> w as UIWindow } }
                .firstOrNull { it.isKeyWindow() }
        var topVC: UIViewController? = keyWindow?.rootViewController
        while (topVC?.presentedViewController != null) {
            topVC = topVC.presentedViewController
        }
        val items: List<Any> =
            if (text.startsWith("http://") || text.startsWith("https://")) {
                listOfNotNull(NSURL(string = text))
            } else {
                listOf(text)
            }
        val activityVC =
            UIActivityViewController(activityItems = items, applicationActivities = null)
        topVC?.presentViewController(activityVC, animated = true, completion = null)
    }
    false
}
