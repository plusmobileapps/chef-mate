package com.plusmobileapps.chefmate.featureflag

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface FeatureFlagsBloc {
    val state: StateFlow<Model>

    fun onBack()

    fun onSetBooleanOverride(flag: BooleanFlag, override: Override<Boolean>)

    fun onSetStringOverride(flag: StringFlag, value: String)

    fun onClearOverride(flag: FeatureFlag<*>)

    fun onClearAllOverrides()

    data class Model(val rows: List<Row> = emptyList())

    data class Row(val flag: FeatureFlag<*>, val resolvedValue: Any, val override: Override<*>)

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): FeatureFlagsBloc
    }
}
