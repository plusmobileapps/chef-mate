package com.plusmobileapps.chefmate.recipe.list.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc.Output
import com.plusmobileapps.chefmate.recipe.list.RecipeListItem
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.MutableStateFlow
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
) : RecipeListBloc,
    BlocContext by context {
    override val state: StateFlow<RecipeListBloc.Model> =
        MutableStateFlow(
            RecipeListBloc.Model(
                recipes = emptyList(),
                isLoading = false,
            ),
        )

    override fun onRecipeClicked(recipe: RecipeListItem) {
        output.onNext(Output.OpenRecipe(recipe.id))
    }

    override fun onAddRecipeClicked() {
        output.onNext(Output.AddNewRecipe)
    }

    override fun onDeleteRecipe(recipe: RecipeListItem) {
        TODO("Not yet implemented")
    }

    override fun onToggleFavorite(recipe: RecipeListItem) {
        TODO("Not yet implemented")
    }
}
