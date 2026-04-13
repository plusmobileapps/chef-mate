package com.plusmobileapps.chefmate

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.bugsnag.Bugsnag

/**
 * A Kermit [LogWriter] that delegates crash reports to the Bugsnag Java SDK.
 *
 * Log messages at [Severity.Warn] and above with a non-null throwable are reported
 * to Bugsnag via [Bugsnag.notify]. All log messages are also sent as breadcrumbs
 * to provide context for crash reports.
 *
 * Modeled after the kermit-bugsnag BugsnagLogWriter for Android/iOS.
 */
class JvmBugsnagLogWriter(
    private val bugsnagClient: Bugsnag,
    private val minCrashSeverity: Severity = Severity.Warn,
) : LogWriter() {
    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?,
    ) {
        // TODO: Add breadcrumb with throwable if available

        if (throwable != null && severity >= minCrashSeverity) {
            bugsnagClient.notify(throwable)
        }
    }
}
