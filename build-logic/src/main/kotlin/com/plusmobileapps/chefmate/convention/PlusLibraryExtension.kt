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
     * Flag to enable Compose multiplatform UI testing. When true, adds `compose.uiTest` to
     * `commonTest`, `compose.desktop.uiTestJUnit4` to `jvmTest`,
     * `androidx.compose.ui:ui-test-junit4-android` and `androidx.test:runner` to
     * `androidInstrumentedTest`, and `androidx.compose.ui:ui-test-manifest` to
     * `debugImplementation`. Also routes the Android target's `commonTest` source set to
     * `androidInstrumentedTest` so `runComposeUiTest`-based tests run on the emulator (via
     * `connectedDebugAndroidTest`) on Android, while continuing to run on JVM and iOS via the usual
     * `jvmTest` / `iosSimulatorArm64Test` tasks.
     *
     * Requires the `compose` convention plugin to also be applied to the module.
     */
    var enableComposeUiTest: Boolean = false
}
