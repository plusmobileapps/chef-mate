package com.plusmobileapps.chefmate.profile.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.profile.ManageProfileBloc
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Model
import com.plusmobileapps.chefmate.profile.ManageProfileBloc.Output
import com.plusmobileapps.chefmate.profile.impl.ui.ManageProfileScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = ManageProfileBloc.Factory::class,
)
class ManageProfileBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<ManageProfileViewModel>,
) : ManageProfileBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<Model> = viewModel.state

    init {
        scope.launch {
            viewModel.outputs.collect {
                when (it) {
                    ManageProfileViewModel.Output.Saved -> output.onNext(Output.Back)
                }
            }
        }
    }

    override fun onBack() {
        output.onNext(Output.Back)
    }

    override fun onDisplayNameChanged(displayName: String) {
        viewModel.setDisplayName(displayName)
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        ManageProfileScreen(bloc = this, modifier = modifier)
    }
}
