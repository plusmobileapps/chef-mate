package com.plusmobileapps.chefmate

import co.touchlab.kermit.Logger
import co.touchlab.kermit.bugsnag.BugsnagLogWriter
import com.bugsnag.kmp.Bugsnag
import com.bugsnag.kmp.Configuration
import com.plusmobileapps.chefmate.buildconfig.BuildConfig

fun initializeBugsnag() {
    BugsnagInitializer().initialize(BuildConfig.BUGSNAG_API_KEY)
}

actual class BugsnagInitializer {
    actual fun initialize(apiKey: String) {
        if (apiKey.isBlank()) return
        val configuration = Configuration(apiKey)
        Bugsnag.start(configuration)
        Logger.addLogWriter(BugsnagLogWriter())
    }
}
