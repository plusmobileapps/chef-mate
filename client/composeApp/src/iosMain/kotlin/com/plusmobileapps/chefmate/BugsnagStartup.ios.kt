package com.plusmobileapps.chefmate

import co.touchlab.kermit.Logger
import co.touchlab.kermit.bugsnag.BugsnagLogWriter
import com.bugsnag.kmp.Bugsnag
import com.bugsnag.kmp.Configuration

actual fun initBugsnag(apiKey: String) {
    if (apiKey.isBlank()) return
    val configuration = Configuration(apiKey)
    Bugsnag.start(configuration)
    Logger.addLogWriter(BugsnagLogWriter())
}
