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

    // Surface the shared controller's active mark so the screen can show one coach mark at a time;
    // only ids in CoachMarkId.recipeDetailSequence are anchored on this screen.
    val state: StateFlow<State> =
        combineStates(_state, coachMarkController.activeCoachMark) { state, activeCoachMark ->
            state.copy(activeCoachMark = activeCoachMark)
        }

    init {
        // Queue every recipe-detail coach mark in order; the controller shows them one at a time.
        CoachMarkId.recipeDetailSequence.forEach(coachMarkController::request)
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
        CoachMarkId.recipeDetailSequence.forEach(coachMarkController::release)
        _output.close()
    }

    fun showGroceryAddedSnackbar() {
        _state.update { it.copy(showGroceryAddedSnackbar = true) }
    }

    fun dismissGroceryAddedSnackbar() {
        _state.update { it.copy(showGroceryAddedSnackbar = false) }
    }

    /**
     * Mark a coach mark seen, but only if it's the one currently showing. This lets a button's tap
     * dismiss its own tip while it's visible, without prematurely consuming tips further down the
     * queue when their button is tapped early.
     */
    fun dismissCoachMark(id: String) {
        if (coachMarkController.activeCoachMark.value == id) {
            coachMarkController.dismiss(id)
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val isDeleting: Boolean = false,
        val showDeleteConfirmationDialog: Boolean = false,
        val recipe: Recipe = Recipe.Empty,
        val showGroceryAddedSnackbar: Boolean = false,
        val activeCoachMark: String? = null,
    )

    sealed class Output {
        data object RecipeDeleted : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(recipeId: Long): RecipeDetailViewModel
    }
}
