package com.plusmobileapps.chefmate.settings.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.settings.AppSettingsBloc
import com.plusmobileapps.chefmate.settings.AppSettingsBloc.Output
import com.plusmobileapps.chefmate.settings.impl.ui.AppSettingsScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AppSettingsBloc.Factory::class,
)
class AppSettingsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<AppSettingsViewModel>,
) : AppSettingsBloc, BlocContext by context {

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

    override fun onImportRecipesClicked() {
        output.onNext(Output.OpenImportRecipes)
    }

    override fun onRecipeCategoriesClicked() {
        output.onNext(Output.OpenRecipeCategories)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        AppSettingsScreen(bloc = this, modifier = modifier)
    }
}
