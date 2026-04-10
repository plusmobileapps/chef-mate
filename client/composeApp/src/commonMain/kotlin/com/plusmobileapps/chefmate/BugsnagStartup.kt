package com.plusmobileapps.chefmate

/**
 * Platform-specific Bugsnag initialization.
 *
 * Each platform should start the Bugsnag SDK and configure Kermit's Logger
 * with the appropriate log writers (including the Bugsnag log writer for
 * crash reporting).
 *
 * @param apiKey The Bugsnag API key from BuildKonfig.
 */
expect fun initBugsnag(apiKey: String)
