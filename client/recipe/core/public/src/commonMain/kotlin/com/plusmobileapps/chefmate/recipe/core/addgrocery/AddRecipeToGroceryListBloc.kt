package com.plusmobileapps.chefmate.recipe.core.addgrocery

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface AddRecipeToGroceryListBloc : BackClickBloc {
    val state: StateFlow<Model>

    fun onIngredientToggled(ingredient: Int)

    fun onSaveClicked()

    data class Model(
        val isLoading: Boolean,
        val isAdding: Boolean,
        val ingredients: List<ListItem>,
    ) {
        data class ListItem(
            val id: Int,
            val name: String,
            val isSelected: Boolean,
        )
    }

    sealed class Output {
        data object Finished : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): AddRecipeToGroceryListBloc
    }
}
