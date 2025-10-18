package com.plusmobileapps.chefmate.recipe.list.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc
import com.plusmobileapps.chefmate.recipe.list.RecipeListBloc.Output
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
    @Assisted private val output: Consumer<Output>
) : RecipeListBloc {
    override val state: StateFlow<RecipeListBloc.Model>
        get() = TODO("Not yet implemented")

    override fun onRecipeClicked(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override fun onAddRecipeClicked() {
        TODO("Not yet implemented")
    }

    override fun onDeleteRecipe(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override fun onToggleFavorite(recipe: Recipe) {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return super.toString()
    }
}