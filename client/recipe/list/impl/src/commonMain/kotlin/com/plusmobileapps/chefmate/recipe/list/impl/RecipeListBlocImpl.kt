package com.plusmobileapps.chefmate.recipe.list.impl

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc.Output
import com.plusmobileapps.chefmate.recipe.list.RecipeListItem
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class RecipeListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: Provider<RecipeListViewModel>,
) : RecipeListBloc,
    BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipeListBlocImpl
    }

    private val viewModel: RecipeListViewModel =
        instanceKeeper.getViewModel {
            viewModelFactory()
        }

    override val state: StateFlow<RecipeListBloc.Model> =
        viewModel.state.mapState {
            RecipeListBloc.Model(
                isLoading = it.isLoading,
                recipes = it.recipes.map { recipe -> recipe.toRecipeListItem() },
            )
        }

    override fun onRecipeClicked(recipe: RecipeListItem) {
        output.onNext(Output.OpenRecipe(recipe.id))
    }

    override fun onAddRecipeClicked() {
        output.onNext(Output.AddNewRecipe)
    }

    override fun onDeleteRecipe(recipe: RecipeListItem) {
        viewModel.deleteRecipe(recipe.id)
    }

    override fun onToggleFavorite(recipe: RecipeListItem) {
        viewModel.toggleFavorite(recipe.id)
    }

    private fun Recipe.toRecipeListItem(): RecipeListItem =
        RecipeListItem(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            starRating = starRating,
        )
}

@ContributesTo(AppScope::class)
interface RecipeListBlocBindingModule {
    @Provides
    fun provideRecipeListBlocFactory(factory: RecipeListBlocImpl.ManagedFactory): RecipeListBloc.Factory =
        object : RecipeListBloc.Factory {
            override fun create(context: BlocContext, output: Consumer<Output>): RecipeListBloc =
                factory.create(context, output)
        }
}
