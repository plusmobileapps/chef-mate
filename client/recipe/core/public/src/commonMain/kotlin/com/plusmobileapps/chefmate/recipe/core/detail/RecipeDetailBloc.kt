package com.plusmobileapps.chefmate.recipe.core.detail

import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.coroutines.flow.StateFlow

interface RecipeDetailBloc : BackClickBloc, BlocScreen {
    val state: StateFlow<Model>

    val childSlot: Value<ChildSlot<*, Sheet>>

    val fullImageSlot: Value<ChildSlot<*, FullImage>>

    fun onImageClicked()

    fun onCloseFullImage()

    fun onEditClicked()

    fun onDeleteClicked()

    fun onDeleteConfirmed()

    fun onDeleteDismissed()

    fun onFavoriteToggled()

    fun onAddToGroceryListClicked()

    fun onCookModeClicked()

    fun onCookModeTooltipDismissed()

    fun onAddToMealPlanClicked()

    fun onSourceUrlClicked(url: String)

    fun onDismissSheet()

    fun onViewGroceryListClicked()

    fun onGrocerySnackbarDismissed()

    data class Model(
        val isLoading: Boolean,
        val isDeleting: Boolean,
        val showDeleteConfirmationDialog: Boolean,
        val recipe: Recipe,
        val createdAt: TextData,
        val updatedAt: TextData,
        val formattedPrepTime: TextData? = null,
        val formattedCookTime: TextData? = null,
        val formattedTotalTime: TextData? = null,
        val showGroceryAddedSnackbar: Boolean = false,
        val showCookModeTooltip: Boolean = false,
    )

    sealed class Output {
        data object Finished : Output()

        data class EditRecipe(val recipeId: Long) : Output()

        data class OpenUrl(val url: String) : Output()

        data object OpenGroceryList : Output()

        data class OpenMealPlanner(val recipeId: Long) : Output()

        data class OpenCookMode(val recipeId: Long) : Output()
    }

    sealed class Sheet {

        abstract val bloc: BlocScreen

        data class AddToGroceryList(override val bloc: AddRecipeToGroceryListBloc) : Sheet()
    }

    sealed class FullImage {
        data class Active(val imageUrl: String, val recipeId: Long, val title: String) : FullImage()
    }

    interface Factory {
        fun create(context: BlocContext, recipeId: Long, output: Consumer<Output>): RecipeDetailBloc
    }
}
