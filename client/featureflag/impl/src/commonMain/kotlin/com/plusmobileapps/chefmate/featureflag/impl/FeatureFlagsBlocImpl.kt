package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc
import com.plusmobileapps.chefmate.featureflag.FeatureFlagsBloc.Output
import com.plusmobileapps.chefmate.featureflag.Override
import com.plusmobileapps.chefmate.featureflag.StringFlag
import com.plusmobileapps.chefmate.getViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class FeatureFlagsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<FeatureFlagsViewModel>,
) : FeatureFlagsBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): FeatureFlagsBlocImpl
    }

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
}

@ContributesTo(AppScope::class)
interface FeatureFlagsBlocBindingModule {
    @Provides
    fun provideFeatureFlagsBlocFactory(
        factory: FeatureFlagsBlocImpl.ManagedFactory
    ): FeatureFlagsBloc.Factory = FeatureFlagsBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
