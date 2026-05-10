package com.plusmobileapps.chefmate.featureflag

import kotlinx.coroutines.flow.StateFlow

interface FeatureFlags {
    fun <T : Any> valueOf(flag: FeatureFlag<T>): StateFlow<T>

    suspend fun refresh()
}

fun FeatureFlags.isEnabled(flag: BooleanFlag): StateFlow<Boolean> = valueOf(flag)
