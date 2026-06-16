package com.plusmobileapps.chefmate

actual class BugsnagInitializer {
    actual fun initialize(apiKey: String) {
        // No Bugsnag SDK for wasmJs; crash reporting on web is out of scope for the spike.
    }
}
