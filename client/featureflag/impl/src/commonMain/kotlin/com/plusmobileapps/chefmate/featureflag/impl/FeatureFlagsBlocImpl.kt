package com.plusmobileapps.chefmate.featureflag.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc.Output
import com.plusmobileapps.chefmate.featureflag.Override
import com.plusmobileapps.chefmate.featureflag.StringFlag
import com.plusmobileapps.chefmate.featureflag.impl.ui.FeatureFlagsScreen
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = FeatureFlagsBloc.Factory::class,
)
class FeatureFlagsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<FeatureFlagsViewModel>,
) : FeatureFlagsBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<FeatureFlagsBloc.Model> = viewModel.state

    override fun onBack() {
        output.onNext(Output.Back)
    }

    override fun onSetBooleanOverride(flag: BooleanFlag, override: Override<Boolean>) {
        viewModel.setBooleanOverride(flag, override)
    }

    override fun onSetStringOverride(flag: StringFlag, value: String) {
        viewModel.setStringOverride(flag, value)
    }

    override fun onClearOverride(flag: FeatureFlag<*>) {
        viewModel.clearOverride(flag)
    }

    override fun onClearAllOverrides() {
        viewModel.clearAll()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        FeatureFlagsScreen(bloc = this, modifier = modifier)
    }
}
