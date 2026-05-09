package com.plusmobileapps.chefmate.featureflag.testing

import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeFeatureFlags(initial: Map<FeatureFlag<*>, Any> = emptyMap()) : FeatureFlags {

    private val flows: MutableMap<FeatureFlag<*>, MutableStateFlow<Any>> =
        initial.mapValues { (_, value) -> MutableStateFlow(value) }.toMutableMap()

    var refreshCount: Int = 0
        private set

    override fun <T : Any> valueOf(flag: FeatureFlag<T>): StateFlow<T> {
        val flow = flows.getOrPut(flag) { MutableStateFlow(flag.defaultValue) }
        @Suppress("UNCHECKED_CAST")
        return flow as StateFlow<T>
    }

    override suspend fun refresh() {
        refreshCount += 1
    }

    fun <T : Any> set(flag: FeatureFlag<T>, value: T) {
        val flow = flows.getOrPut(flag) { MutableStateFlow(value) }
        flow.value = value
    }
}
