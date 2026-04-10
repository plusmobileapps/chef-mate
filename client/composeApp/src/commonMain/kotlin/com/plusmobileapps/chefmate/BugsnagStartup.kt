package com.plusmobileapps.chefmate

/**
 * Platform-specific Bugsnag initializer.
 *
 * Follows the same expect/actual class pattern as [DriverFactory] to allow
 * platform-specific constructor parameters (e.g. Android requires a [Context]).
 *
 * Each platform's actual class starts the Bugsnag SDK and configures Kermit's
 * Logger with the appropriate log writers for crash reporting.
 */
expect class BugsnagInitializer {
    fun initialize(apiKey: String)
}
