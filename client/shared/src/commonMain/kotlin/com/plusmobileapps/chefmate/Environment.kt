package com.plusmobileapps.chefmate

import kotlinx.coroutines.flow.StateFlow

enum class Environment {
    PROD,
    TESTING,
    FAKE,
}

interface EnvironmentProvider {
    val environment: StateFlow<Environment>
}
