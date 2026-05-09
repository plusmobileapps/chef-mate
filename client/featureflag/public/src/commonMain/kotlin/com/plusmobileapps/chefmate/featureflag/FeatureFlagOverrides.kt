package com.plusmobileapps.chefmate.featureflag

import kotlinx.coroutines.flow.StateFlow

sealed interface Override<out T> {
    data object Default : Override<Nothing>

    data class ForceValue<T>(val value: T) : Override<T>
}

interface FeatureFlagOverrides {
    fun <T : Any> overrideOf(flag: FeatureFlag<T>): StateFlow<Override<T>>

    fun <T : Any> setOverride(flag: FeatureFlag<T>, override: Override<T>)

    fun clearAll()
}
