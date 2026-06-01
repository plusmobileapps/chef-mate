package com.plusmobileapps.chefmate.convention

/** Extension for configuring PlusMobile-specific settings. */
open class PlusLibraryExtension {
    /** The namespace to use for the library. If not specified, a default namespace will be used. */
    var namespace: String? = null

    /** Flag to enable Dependency Injection setup. Default is false. */
    var enableDi: Boolean = false

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
