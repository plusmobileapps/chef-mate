package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc
import com.plusmobileapps.chefmate.recipe.core.detail.RecipeDetailBloc.Output
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeDetailBloc.Factory::class,
)
class RecipeDetailBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: (Long) -> RecipeDetailViewModel,
): RecipeDetailBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel: RecipeDetailViewModel = instanceKeeper.getViewModel {
        viewModelFactory(recipeId)
    }

    init {
        scope.launch {
            viewModel.output.collect { viewModelOutput ->
                when (viewModelOutput) {
                    RecipeDetailViewModel.Output.RecipeDeleted -> output.onNext(Output.Finished)
                }
            }
        }
    }

    override val state: StateFlow<RecipeDetailBloc.Model> = viewModel.state.mapState {
        RecipeDetailBloc.Model(
            isLoading = it.isLoading,
            isDeleting = it.isDeleting,
            showDeleteConfirmationDialog = it.showDeleteConfirmationDialog,
            recipe = it.recipe,
        )
    }

    override fun onEditClicked() {
        output.onNext(Output.EditRecipe(recipeId))
    }

    override fun onDeleteClicked() {
        viewModel.showDeleteConfirmationDialog()
    }

    override fun onDeleteConfirmed() {
        viewModel.confirmDelete()
    }

    override fun onDeleteDismissed() {
        viewModel.dismissDeleteConfirmationDialog()
    }

    override fun onFavoriteToggled() {
        viewModel.toggleFavorite()
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }
}