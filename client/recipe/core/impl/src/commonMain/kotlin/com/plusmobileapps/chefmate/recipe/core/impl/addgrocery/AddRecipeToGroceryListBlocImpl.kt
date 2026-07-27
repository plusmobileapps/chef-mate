package com.plusmobileapps.chefmate.recipe.core.impl.addgrocery

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.Output
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AddRecipeToGroceryListBloc.Factory::class,
)
class AddRecipeToGroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: AddRecipeToGroceryListViewModel.Factory,
) : AddRecipeToGroceryListBloc, BlocContext by context {
    private val scope = createScope()

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory.create(recipeId) }

    override val state: StateFlow<AddRecipeToGroceryListBloc.Model> =
        viewModel.state.mapState {
            AddRecipeToGroceryListBloc.Model(
                isLoading = it.isLoading,
                isAdding = it.isAdding,
                groupedIngredients = it.groupedIngredients.toImmutableList(),
                groceryLists = it.groceryLists.toImmutableList(),
                selectedGroceryList = it.selectedGroceryList,
                ingredientScale = it.ingredientScale,
            )
        }

    init {
        scope.launch {
            viewModel.output.collect {
                when (it) {
                    AddRecipeToGroceryListViewModel.Output.Finished -> {
                        output.onNext(Output.Finished)
                    }
                    AddRecipeToGroceryListViewModel.Output.Added -> {
                        output.onNext(Output.Added)
                    }
                }
            }
        }
    }

    override fun onIngredientToggled(ingredient: Int) {
        viewModel.toggleIngredient(ingredient)
    }

    override fun onGroceryListSelected(listId: Long) {
        viewModel.onGroceryListSelected(listId)
    }

    override fun onScaleChanged(scale: Double) {
        viewModel.setScale(scale)
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }
}
