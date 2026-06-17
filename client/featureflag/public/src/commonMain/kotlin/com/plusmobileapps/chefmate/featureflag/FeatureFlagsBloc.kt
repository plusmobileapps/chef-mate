package com.plusmobileapps.chefmate.featureflag

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.featureflag.ui.FeatureFlagsScreen
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface FeatureFlagsBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        FeatureFlagsScreen(bloc = this, modifier = modifier)
    }

    fun onBack()

    fun onSetBooleanOverride(flag: BooleanFlag, override: Override<Boolean>)

    fun onSetStringOverride(flag: StringFlag, value: String)

    fun onClearOverride(flag: FeatureFlag<*>)

    fun onClearAllOverrides()

    data class Model(val rows: ImmutableList<Row> = persistentListOf())

    data class Row(val flag: FeatureFlag<*>, val resolvedValue: Any, val override: Override<*>)

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): FeatureFlagsBloc
    }
}
