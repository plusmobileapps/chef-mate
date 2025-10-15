package com.plusmobileapps.chefmate.recipe.list

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.data.Recipe
import kotlinx.coroutines.flow.StateFlow

interface RecipeListBloc {

    val state: StateFlow<Model>

    fun onRecipeClicked(recipe: Recipe)

    fun onAddRecipeClicked()

    fun onDeleteRecipe(recipe: Recipe)

    fun onToggleFavorite(recipe: Recipe)

    data class Model(
        val recipes: List<Recipe> = emptyList(),
        val isLoading: Boolean = false,
    )

    sealed class Output {
        data class OpenRecipe(val recipeId: Long) : Output()
        object AddNewRecipe : Output()
    }

    interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipeListBloc
    }
}