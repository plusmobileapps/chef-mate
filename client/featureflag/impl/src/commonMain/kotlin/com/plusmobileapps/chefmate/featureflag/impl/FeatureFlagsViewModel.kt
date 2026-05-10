package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlagOverrides
import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.featureflag.FeatureFlags
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.featureflag.Override
import com.plusmobileapps.chefmate.featureflag.StringFlag
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@Inject
class FeatureFlagsViewModel(
    @Main mainContext: CoroutineContext,
    private val featureFlags: FeatureFlags,
    private val overrides: FeatureFlagOverrides,
) : ViewModel(mainContext) {

    val state: StateFlow<FeatureFlagsBloc.Model> = buildState()

    private fun buildState(): StateFlow<FeatureFlagsBloc.Model> {
        val rowFlows: List<Flow<FeatureFlagsBloc.Row>> = FeatureFlagRegistry.all.map { rowFor(it) }
        val combined: Flow<FeatureFlagsBloc.Model> =
            if (rowFlows.isEmpty()) {
                flowOf(FeatureFlagsBloc.Model())
            } else {
                combine(rowFlows) { rows -> FeatureFlagsBloc.Model(rows = rows.toList()) }
            }
        return combined.stateIn(scope, SharingStarted.Eagerly, FeatureFlagsBloc.Model())
    }

    private fun <T : Any> rowFor(flag: FeatureFlag<T>): Flow<FeatureFlagsBloc.Row> =
        combine(featureFlags.valueOf(flag), overrides.overrideOf(flag)) { value, override ->
            FeatureFlagsBloc.Row(flag = flag, resolvedValue = value, override = override)
        }

    fun setBooleanOverride(flag: BooleanFlag, override: Override<Boolean>) {
        overrides.setOverride(flag, override)
    }

    fun setStringOverride(flag: StringFlag, value: String) {
        overrides.setOverride(flag, Override.ForceValue(value))
    }

    fun clearOverride(flag: FeatureFlag<*>) {
        when (flag) {
            is BooleanFlag -> overrides.setOverride(flag, Override.Default)
            is StringFlag -> overrides.setOverride(flag, Override.Default)
        }
    }

    fun clearAll() {
        overrides.clearAll()
    }
}
