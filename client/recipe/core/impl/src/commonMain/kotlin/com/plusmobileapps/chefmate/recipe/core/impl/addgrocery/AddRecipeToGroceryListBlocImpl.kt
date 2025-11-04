package com.plusmobileapps.chefmate.recipe.core.impl.addgrocery

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.Output
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AddRecipeToGroceryListBloc.Factory::class,
)
class AddRecipeToGroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: (Long) -> AddRecipeToGroceryListViewModel,
) : AddRecipeToGroceryListBloc,
    BlocContext by context {
    private val scope = createScope()

    private val viewModel =
        instanceKeeper.getViewModel {
            viewModelFactory(recipeId)
        }

    override val state: StateFlow<AddRecipeToGroceryListBloc.Model> =
        viewModel.state.mapState {
            AddRecipeToGroceryListBloc.Model(
                isLoading = it.isLoading,
                isAdding = it.isAdding,
                ingredients = it.ingredients,
            )
        }

    init {
        scope.launch {
            viewModel.output.collect {
                when (it) {
                    AddRecipeToGroceryListViewModel.Output.Finished -> {
                        output.onNext(Output.Finished)
                    }
                }
            }
        }
    }

    override fun onIngredientToggled(ingredient: Int) {
        viewModel.toggleIngredient(ingredient)
    }

    override fun onSaveClicked() {
        viewModel.save()
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }
}
