package com.plusmobileapps.chefmate.recipe.core.impl.addgrocery

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.Output
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
class AddRecipeToGroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val recipeId: Long,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: AddRecipeToGroceryListViewModel.Factory,
) : AddRecipeToGroceryListBloc,
    BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): AddRecipeToGroceryListBlocImpl
    }

    private val scope = createScope()

    private val viewModel =
        instanceKeeper.getViewModel {
            viewModelFactory.create(recipeId)
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

@ContributesTo(AppScope::class)
interface AddRecipeToGroceryListBlocBindingModule {
    @Provides
    fun provideAddRecipeToGroceryListBlocFactory(
        factory: AddRecipeToGroceryListBlocImpl.ManagedFactory,
    ): AddRecipeToGroceryListBloc.Factory =
        AddRecipeToGroceryListBloc.Factory { context, recipeId, output -> factory.create(context, recipeId, output) }
}
