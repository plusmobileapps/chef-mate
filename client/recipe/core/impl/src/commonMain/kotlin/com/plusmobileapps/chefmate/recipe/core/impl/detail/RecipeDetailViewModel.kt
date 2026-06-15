package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.combineStates
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class RecipeDetailViewModel(
    @Assisted private val recipeId: Long,
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
    private val coachMarkController: CoachMarkController,
) : ViewModel(mainContext) {

    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()

    private val _state = MutableStateFlow(State())

    // The cook-mode coach mark only shows while it's the active mark in the shared controller, so
    // at most one coach mark is ever visible across the app at a time.
    val state: StateFlow<State> =
        combineStates(_state, coachMarkController.activeCoachMark) { state, activeCoachMark ->
            state.copy(showCookModeTooltip = activeCoachMark == CoachMarkId.RECIPE_DETAIL_COOK_MODE)
        }

    init {
        coachMarkController.request(CoachMarkId.RECIPE_DETAIL_COOK_MODE)
        scope.launch { observeRecipe() }
    }

    private suspend fun observeRecipe() {
        repository.getRecipe(recipeId).collect { recipe ->
            _state.update { it.copy(isLoading = false, recipe = recipe ?: Recipe.Empty) }
        }
    }

    fun showDeleteConfirmationDialog() {
        _state.update { it.copy(showDeleteConfirmationDialog = true) }
    }

    fun dismissDeleteConfirmationDialog() {
        _state.update { it.copy(showDeleteConfirmationDialog = false) }
    }

    fun confirmDelete() {
        _state.update { it.copy(showDeleteConfirmationDialog = false, isDeleting = true) }
        scope.launch {
            repository.deleteRecipe(recipeId)
            _output.send(Output.RecipeDeleted)
        }
    }

    fun toggleFavorite() {
        scope.launch {
            val recipe = repository.getRecipe(recipeId).first() ?: return@launch
            repository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Leaving the screen without dismissing frees the queue so other coach marks can show.
        coachMarkController.release(CoachMarkId.RECIPE_DETAIL_COOK_MODE)
        _output.close()
    }

    fun showGroceryAddedSnackbar() {
        _state.update { it.copy(showGroceryAddedSnackbar = true) }
    }

    fun dismissGroceryAddedSnackbar() {
        _state.update { it.copy(showGroceryAddedSnackbar = false) }
    }

    /** Hide the cook-mode coach mark and remember the dismissal so it never shows again. */
    fun dismissCookModeTooltip() {
        coachMarkController.dismiss(CoachMarkId.RECIPE_DETAIL_COOK_MODE)
    }

    data class State(
        val isLoading: Boolean = true,
        val isDeleting: Boolean = false,
        val showDeleteConfirmationDialog: Boolean = false,
        val recipe: Recipe = Recipe.Empty,
        val showGroceryAddedSnackbar: Boolean = false,
        val showCookModeTooltip: Boolean = false,
    )

    sealed class Output {
        data object RecipeDeleted : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(recipeId: Long): RecipeDetailViewModel
    }
}
