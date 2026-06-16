package com.plusmobileapps.chefmate

enum class Platform {
    ANDROID,
    IOS,
    JVM,
    WASM,
}

expect val currentPlatform: Platform
