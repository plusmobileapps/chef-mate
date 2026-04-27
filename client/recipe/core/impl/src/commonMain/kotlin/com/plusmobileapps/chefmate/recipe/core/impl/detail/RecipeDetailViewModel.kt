package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class RecipeDetailViewModel(
    @Assisted private val recipeId: Long,
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
    settings: Settings,
) : ViewModel(mainContext) {

    private var keepScreenOnPref by settings.boolean(KEY_KEEP_SCREEN_ON, defaultValue = true)
    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()

    private val _state = MutableStateFlow(State(keepScreenOn = keepScreenOnPref))
    val state: StateFlow<State> = _state.asStateFlow()

    init {
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
        _output.close()
    }

    fun showGroceryAddedSnackbar() {
        _state.update { it.copy(showGroceryAddedSnackbar = true) }
    }

    fun dismissGroceryAddedSnackbar() {
        _state.update { it.copy(showGroceryAddedSnackbar = false) }
    }

    fun toggleKeepScreenOn() {
        val newValue = !_state.value.keepScreenOn
        keepScreenOnPref = newValue
        _state.update { it.copy(keepScreenOn = newValue) }
    }

    data class State(
        val isLoading: Boolean = true,
        val isDeleting: Boolean = false,
        val showDeleteConfirmationDialog: Boolean = false,
        val recipe: Recipe = Recipe.Empty,
        val showGroceryAddedSnackbar: Boolean = false,
        val keepScreenOn: Boolean = true,
    )

    sealed class Output {
        data object RecipeDeleted : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(recipeId: Long): RecipeDetailViewModel
    }

    companion object {
        private const val KEY_KEEP_SCREEN_ON = "recipe_detail_keep_screen_on"
    }
}
