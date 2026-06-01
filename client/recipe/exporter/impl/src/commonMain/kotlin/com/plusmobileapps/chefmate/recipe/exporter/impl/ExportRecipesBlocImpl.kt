package com.plusmobileapps.chefmate.recipe.exporter.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Output
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = ExportRecipesBloc.Factory::class,
)
class ExportRecipesBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<ExportRecipesViewModel>,
) : ExportRecipesBloc, BlocContext by context {

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<ExportRecipesBloc.Model> = viewModel.state

    override fun onRecipeToggled(id: String) = viewModel.onRecipeToggled(id)

    override fun onToggleSelectAll() = viewModel.onToggleSelectAll()

    override fun onExportClicked() = viewModel.onExportClicked()

    override fun onSaveCompleted(saved: Boolean) = viewModel.onSaveCompleted(saved)

    override fun onStartOver() = viewModel.onStartOver()

    override fun onBack() {
        output.onNext(Output.Back)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        ExportRecipesScreen(bloc = this, modifier = modifier)
    }
}
