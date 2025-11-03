package com.plusmobileapps.chefmate.recipe.core.impl.edit

import chefmate.client.recipe.core.impl.generated.resources.Res
import chefmate.client.recipe.core.impl.generated.resources.create_recipe
import chefmate.client.recipe.core.impl.generated.resources.edit_recipe
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc
import com.plusmobileapps.chefmate.recipe.core.edit.EditRecipeBloc.Output
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = EditRecipeBloc.Factory::class,
)
class EditRecipeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted recipeId: Long?,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: (Long?) -> EditRecipeViewModel,
) : EditRecipeBloc,
    BlocContext by context {
    private val scope = createScope()

    private val viewModel: EditRecipeViewModel =
        instanceKeeper.getViewModel {
            viewModelFactory(recipeId)
        }

    override val state: StateFlow<EditRecipeBloc.Model> =
        viewModel.state.mapState {
            EditRecipeBloc.Model(
                title =
                    if (recipeId != null) {
                        ResourceString(Res.string.edit_recipe)
                    } else {
                        ResourceString(Res.string.create_recipe)
                    },
                isLoading = it.isLoading,
                isSaving = it.isSaving,
                showDiscardChangesDialog = it.showDiscardChangesDialog,
            )
        }
    override val title: StateFlow<String> = viewModel.title
    override val description: StateFlow<String> = viewModel.description
    override val imageUrl: StateFlow<String> = viewModel.imageUrl
    override val ingredients: StateFlow<String> = viewModel.ingredients
    override val directions: StateFlow<String> = viewModel.directions
    override val sourceUrl: StateFlow<String> = viewModel.sourceUrl
    override val servings: StateFlow<String> = viewModel.servings
    override val prepTime: StateFlow<String> = viewModel.prepTime
    override val cookTime: StateFlow<String> = viewModel.cookTime
    override val totalTime: StateFlow<String> = viewModel.totalTime
    override val calories: StateFlow<String> = viewModel.calories
    override val starRating: StateFlow<Int?> = viewModel.starRating

    init {
        scope.launch {
            viewModel.output.collect {
                when (it) {
                    is EditRecipeViewModel.Output.Finished -> {
                        output.onNext(Output.Finished(it.recipeId))
                    }

                    EditRecipeViewModel.Output.Cancelled -> {
                        output.onNext(Output.Cancelled)
                    }
                }
            }
        }
    }

    override fun onTitleChanged(title: String) {
        viewModel.updateTitle(title)
    }

    override fun onDescriptionChanged(description: String) {
        viewModel.updateDescription(description)
    }

    override fun onImageUrlChanged(imageUrl: String) {
        viewModel.updateImageUrl(imageUrl)
    }

    override fun onIngredientsChanged(ingredients: String) {
        viewModel.updateIngredients(ingredients)
    }

    override fun onDirectionsChanged(directions: String) {
        viewModel.updateDirections(directions)
    }

    override fun onSourceUrlChanged(sourceUrl: String) {
        viewModel.updateSourceUrl(sourceUrl)
    }

    override fun onServingsChanged(servings: String) {
        viewModel.updateServings(servings)
    }

    override fun onPrepTimeChanged(prepTime: String) {
        viewModel.updatePrepTime(prepTime)
    }

    override fun onCookTimeChanged(cookTime: String) {
        viewModel.updateCookTime(cookTime)
    }

    override fun onTotalTimeChanged(totalTime: String) {
        viewModel.updateTotalTime(totalTime)
    }

    override fun onCaloriesChanged(calories: String) {
        viewModel.updateCalories(calories)
    }

    override fun onStarRatingChanged(starRating: Int?) {
        viewModel.updateStarRating(starRating)
    }

    override fun onDiscardChangesConfirmed() {
        output.onNext(Output.Cancelled)
    }

    override fun onDiscardChangesCancelled() {
        viewModel.dismissDiscardChangesDialog()
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    override fun onBackClicked() {
        viewModel.tryToClose()
    }
}
