package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlagOverrides
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.featureflag.Override
import com.plusmobileapps.chefmate.featureflag.StringFlag
import com.russhwolf.settings.Settings
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class FeatureFlagOverridesImpl(private val settings: Settings) : FeatureFlagOverrides {

    private val states: MutableMap<String, MutableStateFlow<Override<*>>> =
        FeatureFlagRegistry.all
            .associate { it.key to MutableStateFlow<Override<*>>(loadOverride(it)) }
            .toMutableMap()

    override fun <T : Any> overrideOf(flag: FeatureFlag<T>): StateFlow<Override<T>> {
        val flow = states.getOrPut(flag.key) { MutableStateFlow(loadOverride(flag)) }
        @Suppress("UNCHECKED_CAST")
        return flow.asStateFlow() as StateFlow<Override<T>>
    }

    override fun <T : Any> setOverride(flag: FeatureFlag<T>, override: Override<T>) {
        when (override) {
            Override.Default -> {
                settings.remove(presentKey(flag.key))
                settings.remove(valueKey(flag.key))
            }
            is Override.ForceValue -> {
                settings.putBoolean(presentKey(flag.key), true)
                settings.putString(valueKey(flag.key), override.value.toString())
            }
        }
        val flow = states.getOrPut(flag.key) { MutableStateFlow(Override.Default) }
        flow.value = override
    }

    override fun clearAll() {
        FeatureFlagRegistry.all.forEach { flag ->
            settings.remove(presentKey(flag.key))
            settings.remove(valueKey(flag.key))
            states[flag.key]?.value = Override.Default
        }
    }

    private fun <T : Any> loadOverride(flag: FeatureFlag<T>): Override<T> {
        val present = settings.getBooleanOrNull(presentKey(flag.key)) ?: false
        if (!present) return Override.Default
        val raw = settings.getStringOrNull(valueKey(flag.key)) ?: return Override.Default
        @Suppress("UNCHECKED_CAST")
        return when (flag) {
            is BooleanFlag ->
                Override.ForceValue(raw.equals("true", ignoreCase = true)) as Override<T>
            is StringFlag -> Override.ForceValue(raw) as Override<T>
        }
    }

    private fun presentKey(key: String): String = "$KEY_PREFIX$key.present"

    private fun valueKey(key: String): String = "$KEY_PREFIX$key.value"

    private companion object {
        const val KEY_PREFIX = "flags.override."
    }
}
