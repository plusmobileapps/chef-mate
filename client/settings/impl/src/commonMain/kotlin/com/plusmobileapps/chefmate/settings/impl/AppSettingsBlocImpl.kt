package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc.Output
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class AppSettingsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<AppSettingsViewModel>,
) : AppSettingsBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): AppSettingsBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<AppSettingsBloc.Model> = viewModel.state

    override fun onBack() {
        output.onNext(Output.Back)
    }

    override fun onHistoryEnabledChanged(enabled: Boolean) {
        viewModel.setHistoryEnabled(enabled)
    }

    override fun onClearHistoryClicked() {
        viewModel.showClearHistoryDialog()
    }

    override fun onClearHistoryConfirmed() {
        viewModel.clearHistory()
    }

    override fun onClearHistoryDismissed() {
        viewModel.dismissClearHistoryDialog()
    }

    override fun onBottomNavOrderClicked() {
        output.onNext(Output.OpenBottomNavOrder)
    }
}

@ContributesTo(AppScope::class)
interface AppSettingsBlocBindingModule {
    @Provides
    fun provideAppSettingsBlocFactory(
        factory: AppSettingsBlocImpl.ManagedFactory
    ): AppSettingsBloc.Factory = AppSettingsBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
