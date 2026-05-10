package com.plusmobileapps.chefmate

enum class Platform {
    ANDROID,
    IOS,
    JVM,
}

expect val currentPlatform: Platform
