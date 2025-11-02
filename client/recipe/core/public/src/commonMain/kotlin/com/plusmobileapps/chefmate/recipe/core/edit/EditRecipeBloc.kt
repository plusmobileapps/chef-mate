package com.plusmobileapps.chefmate.recipe.core.edit

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface EditRecipeBloc : BackClickBloc {
    val state: StateFlow<Model>

    val title: StateFlow<String>

    val description: StateFlow<String>

    val imageUrl: StateFlow<String>

    val ingredients: StateFlow<String>

    val directions: StateFlow<String>

    val sourceUrl: StateFlow<String>

    val servings: StateFlow<String>

    val prepTime: StateFlow<String>

    val cookTime: StateFlow<String>

    val totalTime: StateFlow<String>

    val calories: StateFlow<String>

    val starRating: StateFlow<Int?>

    fun onTitleChanged(title: String)

    fun onDescriptionChanged(description: String)

    fun onImageUrlChanged(imageUrl: String)

    fun onIngredientsChanged(ingredients: String)

    fun onDirectionsChanged(directions: String)

    fun onSourceUrlChanged(sourceUrl: String)

    fun onServingsChanged(servings: String)

    fun onPrepTimeChanged(prepTime: String)

    fun onCookTimeChanged(cookTime: String)

    fun onTotalTimeChanged(totalTime: String)

    fun onCaloriesChanged(calories: String)

    fun onStarRatingChanged(starRating: Int?)

    fun onDiscardChangesConfirmed()

    fun onDiscardChangesCancelled()

    fun onSaveClicked()

    data class Model(
        val isLoading: Boolean,
        val isSaving: Boolean,
        val showDiscardChangesDialog: Boolean,
    )

    sealed class Output {
        data class Finished(
            val recipeId: Long,
        ) : Output()

        data object Cancelled : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            recipeId: Long?,
            output: Consumer<Output>,
        ): EditRecipeBloc
    }
}
