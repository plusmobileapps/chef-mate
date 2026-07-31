package com.plusmobileapps.chefmate.profile.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.ChefMateUrls
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.profile.ProfileBloc
import com.plusmobileapps.chefmate.profile.ProfileBloc.Model
import com.plusmobileapps.chefmate.profile.ProfileBloc.Output
import com.plusmobileapps.chefmate.profile.ProfileBloc.Props
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = ProfileBloc.Factory::class)
class ProfileBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val props: Props,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: ProfileViewModel.Factory,
) : ProfileBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory.create(props) }

    private val _shareLink = Channel<String>(Channel.BUFFERED)

    override val state: StateFlow<Model> = viewModel.state

    override val shareLink: Flow<String> = _shareLink.receiveAsFlow()

    override fun onRecipeClicked(remoteId: String) {
        output.onNext(Output.OpenRecipe(remoteId))
    }

    override fun onManageProfileClicked() {
        output.onNext(Output.OpenManageProfile)
    }

    override fun onCreateProfileClicked() {
        // Claiming a handle happens in the profile editor, so both routes land on the same screen.
        output.onNext(Output.OpenManageProfile)
    }

    override fun onShareClicked() {
        val handle = viewModel.loadedHandleOrNull() ?: return
        scope.launch { _shareLink.send(ChefMateUrls.profileShareUrl(handle)) }
    }

    override fun onRetryClicked() = viewModel.retry()

    override fun onBackClicked() {
        output.onNext(Output.Back)
    }
}
