package com.plusmobileapps.chefmate.recipe.core.impl.share

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.recipe.core.share.PublicRecipeBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = PublicRecipeBloc.Factory::class,
)
class PublicRecipeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val remoteId: String,
    @Assisted private val output: Consumer<PublicRecipeBloc.Output>,
    private val viewModelFactory: PublicRecipeViewModel.Factory,
) : PublicRecipeBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel: PublicRecipeViewModel = instanceKeeper.getViewModel {
        viewModelFactory.create(remoteId)
    }

    init {
        scope.launch {
            viewModel.output.collect { viewModelOutput ->
                when (viewModelOutput) {
                    is PublicRecipeViewModel.Output.OpenRecipe ->
                        output.onNext(PublicRecipeBloc.Output.OpenRecipe(viewModelOutput.recipeId))
                }
            }
        }
    }

    override val state: StateFlow<PublicRecipeBloc.Model> = viewModel.state

    override fun onSaveClicked() = viewModel.onSave()

    override fun onRetryClicked() = viewModel.onRetry()

    override fun onSourceUrlClicked(url: String) {
        output.onNext(PublicRecipeBloc.Output.OpenUrl(url))
    }

    override fun onBackClicked() {
        output.onNext(PublicRecipeBloc.Output.Finished)
    }
}
