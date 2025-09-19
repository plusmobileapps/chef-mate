package com.plusmobileapps.chefmate.convention

/**
 * Extension for configuring PlusMobile-specific settings.
 */
open class PlusLibraryExtension {
    /**
     * The namespace to use for the library. If not specified, a default namespace will be used.
     */
    var namespace: String? = null

    /**
     * Flag to enable Dependency Injection setup. Default is false.
     */
    var enableDi: Boolean = false

    /**
     * Flag to enable test dependencies and configurations. Default is false.
     */
    var enableTesting: Boolean = false
}


