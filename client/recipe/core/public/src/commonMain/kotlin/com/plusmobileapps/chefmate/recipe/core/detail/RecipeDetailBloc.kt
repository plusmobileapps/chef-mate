package com.plusmobileapps.chefmate.recipe.core.detail

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow

interface RecipeDetailBloc : BackClickBloc {
    val state: StateFlow<Model>

    val childSlot: Value<ChildSlot<*, Sheet>>

    fun onEditClicked()

    fun onDeleteClicked()

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    fun onFavoriteToggled()

    fun onAddToGroceryListClicked()

    data class Model(
        val isLoading: Boolean,
        val isDeleting: Boolean,
        val showDeleteConfirmationDialog: Boolean,
        val recipe: Recipe,
        val createdAt: TextData,
        val updatedAt: TextData,
    )

    sealed class Output {
        data object Finished : Output()

        data class EditRecipe(
            val recipeId: Long,
        ) : Output()
    }

    sealed class Sheet {
        data class AddToGroceryList(
            val bloc: AddRecipeToGroceryListBloc,
        ) : Sheet()
    }

    interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long,
            output: Consumer<Output>,
        ): RecipeDetailBloc
    }
}
