package com.plusmobileapps.chefmate.convention

/**
 * Configuration for the `com.plusmobileapps.chefmate.application` (plusApplication) convention
 * plugin used by standalone feature demo apps, e.g. `:client:settings:demo`.
 */
open class PlusApplicationExtension {
    /**
     * Android namespace + `applicationId` default, e.g.
     * `com.plusmobileapps.chefmate.settings.demo`.
     */
    var namespace: String? = null

    /** Android `applicationId`. Defaults to [namespace] when unset. */
    var applicationId: String? = null

    /** Wire up Metro DI (KSP + metro-extensions). On by default — demos run the real BLoCs. */
    var enableDi: Boolean = true

    /** Add the shared testing dependencies (mokkery, turbine, kotest) to `commonTest`. */
    var enableTesting: Boolean = false
}
