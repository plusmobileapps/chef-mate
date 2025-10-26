package com.plusmobileapps.chefmate.recipe.core.detail

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.Recipe
import kotlinx.coroutines.flow.StateFlow

interface RecipeDetailBloc {
    val state: StateFlow<Model>

    fun onEditClicked()

    fun onDeleteClicked()

    fun onFavoriteToggled()

    data class Model(
        val isLoading: Boolean,
        val recipe: Recipe,
    )

    sealed class Output {
        data object Finished : Output()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): RecipeDetailBloc
    }
}
