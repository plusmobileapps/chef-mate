package com.plusmobileapps.chefmate

import co.touchlab.kermit.Logger
import com.bugsnag.Bugsnag

actual class BugsnagInitializer {
    actual fun initialize(apiKey: String) {
        if (apiKey.isBlank()) return
        val bugsnagClient = Bugsnag(apiKey)
        Logger.addLogWriter(JvmBugsnagLogWriter(bugsnagClient))
    }
}
