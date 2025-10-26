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

    fun onTitleChanged(title: String)

    fun onDescriptionChanged(description: String)

    fun onImageUrlChanged(imageUrl: String)

    fun onIngredientsChanged(ingredients: String)

    fun onDirectionsChanged(directions: String)

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
