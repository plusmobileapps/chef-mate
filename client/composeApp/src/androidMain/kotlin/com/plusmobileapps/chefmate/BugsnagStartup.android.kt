package com.plusmobileapps.chefmate

import android.content.Context
import co.touchlab.kermit.Logger
import co.touchlab.kermit.bugsnag.BugsnagLogWriter
import com.bugsnag.kmp.Bugsnag
import com.bugsnag.kmp.Configuration

actual class BugsnagInitializer(private val context: Context) {
    actual fun initialize(apiKey: String) {
        if (apiKey.isBlank()) return
        val configuration = Configuration(context, apiKey)
        Bugsnag.start(configuration)
        Logger.addLogWriter(BugsnagLogWriter())
    }
}
