package com.plusmobileapps.chefmate.convention

/** Extension for configuring PlusMobile-specific settings. */
open class PlusLibraryExtension(
    /**
     * Invoked the moment [enableWatch] is set to `true` from a module's `plusLibrary { }` block.
     * Runs during project configuration (before `afterEvaluate`) — the valid window to register
     * additional Kotlin Multiplatform targets. Supplied by [KmpLibraryConventionPlugin].
     */
    private val onWatchEnabled: () -> Unit = {}
) {
    /** The namespace to use for the library. If not specified, a default namespace will be used. */
    var namespace: String? = null

    /** Flag to enable Dependency Injection setup. Default is false. */
    var enableDi: Boolean = false

    /**
     * Flag to add the watchOS targets (`watchosArm64` + `watchosSimulatorArm64`) to this module.
     * Only opt in on Compose-free, pure-Kotlin modules — the Compose Multiplatform runtime has no
     * watchOS target, so enabling this on a `compose`-plugin module will fail to compile. Shared
     * Darwin/native dependencies and `expect`/`actual` files must live in the `appleMain` source
     * set so both iOS and watchOS pick them up. Setting this to `true` registers the targets
     * immediately via [onWatchEnabled].
     */
    var enableWatch: Boolean = false
        set(value) {
            field = value
            if (value) onWatchEnabled()
        }

    /** Flag to enable test dependencies and configurations. Default is false. */
    var enableTesting: Boolean = false

    /**
     * Flag to enable database testing support. When true, adds the `database:testing` dependency to
     * commonTest and links sqlite3 for iOS test binaries. Requires [enableTesting] to also be true.
     */
    var enableDatabaseTesting: Boolean = false

    /**
     * Flag to enable the Compose UI test dependency. When true, adds `compose.uiTest` as an `api`
     * dependency on `commonMain`. Intended for `impl-robots` modules that expose reusable Compose
     * UI test robots. Requires the `compose` plugin to also be applied on the module.
     */
    var uiTest: Boolean = false
}
