package com.plusmobileapps.chefmate.meal.core.impl.recipepicker

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.meal.core.recipepicker.RecipePickerBloc
import com.plusmobileapps.chefmate.meal.core.recipepicker.RecipePickerBloc.Output
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class RecipePickerBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: Provider<RecipePickerViewModel>,
) : RecipePickerBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipePickerBlocImpl
    }

    private val viewModel: RecipePickerViewModel = instanceKeeper.getViewModel {
        viewModelFactory()
    }

    override val state: StateFlow<RecipePickerBloc.Model> =
        viewModel.state.mapState {
            RecipePickerBloc.Model(
                recipes = it.displayRecipes,
                searchQuery = it.searchQuery,
                isLoading = it.isLoading,
            )
        }

    override fun onSearchQueryChanged(query: String) {
        viewModel.updateSearchQuery(query)
    }

    override fun onRecipeSelected(item: RecipePickerBloc.RecipePickerItem) {
        output.onNext(Output.RecipeSelected(recipeId = item.id))
    }
}

@ContributesTo(AppScope::class)
interface RecipePickerBlocBindingModule {
    @Provides
    fun provideRecipePickerBlocFactory(
        factory: RecipePickerBlocImpl.ManagedFactory
    ): RecipePickerBloc.Factory = RecipePickerBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
