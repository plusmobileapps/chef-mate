package com.plusmobileapps.chefmate.recipe.list.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc.Output
import com.plusmobileapps.chefmate.recipe.list.RecipeListItem
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = RecipeListBloc.Factory::class,
)
class RecipeListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: () -> RecipeListViewModel,
) : RecipeListBloc,
    BlocContext by context {
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
