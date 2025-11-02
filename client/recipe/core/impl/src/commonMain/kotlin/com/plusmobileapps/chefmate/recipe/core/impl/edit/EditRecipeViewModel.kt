@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.edit

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Inject
class EditRecipeViewModel(
    @Assisted private val recipeId: Long?,
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
) : ViewModel(mainContext) {
    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _ingredients = MutableStateFlow("")
    val ingredients: StateFlow<String> = _ingredients.asStateFlow()

    private val _directions = MutableStateFlow("")
    val directions: StateFlow<String> = _directions.asStateFlow()

    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl.asStateFlow()

    private val _sourceUrl = MutableStateFlow("")
    val sourceUrl: StateFlow<String> = _sourceUrl.asStateFlow()

    private val _servings = MutableStateFlow("")
    val servings: StateFlow<String> = _servings.asStateFlow()

    private val _prepTime = MutableStateFlow("")
    val prepTime: StateFlow<String> = _prepTime.asStateFlow()

    private val _cookTime = MutableStateFlow("")
    val cookTime: StateFlow<String> = _cookTime.asStateFlow()

    private val _totalTime = MutableStateFlow("")
    val totalTime: StateFlow<String> = _totalTime.asStateFlow()

    private val _calories = MutableStateFlow("")
    val calories: StateFlow<String> = _calories.asStateFlow()

    private val _starRating = MutableStateFlow<Int?>(null)
    val starRating: StateFlow<Int?> = _starRating.asStateFlow()

    init {
        if (recipeId != null) {
            _state.update { it.copy(isLoading = false) }
            scope.launch {
                loadRecipe(recipeId)
            }
        }
    }

    fun updateTitle(value: String) {
        _title.value = value
    }

    fun updateDescription(value: String) {
        _description.value = value
    }

    fun updateIngredients(value: String) {
        _ingredients.value = value
    }

    fun updateDirections(value: String) {
        _directions.value = value
    }

    fun updateImageUrl(value: String) {
        _imageUrl.value = value
    }

    fun updateSourceUrl(value: String) {
        _sourceUrl.value = value
    }

    fun updateServings(value: String) {
        _servings.value = value
    }

    fun updatePrepTime(value: String) {
        _prepTime.value = value
    }

    fun updateCookTime(value: String) {
        _cookTime.value = value
    }

    fun updateTotalTime(value: String) {
        _totalTime.value = value
    }

    fun updateCalories(value: String) {
        _calories.value = value
    }

    fun updateStarRating(value: Int?) {
        _starRating.value = value
    }

    fun tryToClose() {
        val originalRecipe = _state.value.recipe
        val currentRecipe = currentRecipe()
        when {
            shouldShowDiscardChangesDialog(originalRecipe, currentRecipe) -> {
                _state.update { it.copy(showDiscardChangesDialog = true) }
            }
            else -> {
                scope.launch {
                    _output.send(Output.Cancelled)
                }
            }
        }
    }

    fun dismissDiscardChangesDialog() {
        _state.update { it.copy(showDiscardChangesDialog = false) }
    }

    fun save() {
        val originalRecipe = _state.value.recipe
        val currentRecipe = currentRecipe()
        _state.update { it.copy(isLoading = true) }
        scope.launch {
            val savedRecipe = if (originalRecipe != null) {
                repository.updateRecipe(currentRecipe)
            } else {
                repository.createRecipe(currentRecipe)
            }
            _output.send(Output.Finished(savedRecipe.id))
        }
    }

    override fun onCleared() {
        super.onCleared()
        _output.close()
    }

    private suspend fun loadRecipe(id: Long) {
        val recipe = repository.getRecipe(id).first()
        if (recipe == null) {
            _output.send(Output.Cancelled)
            return
        }
        _title.value = recipe.title
        _description.value = recipe.description.orEmpty()
        _ingredients.value = recipe.ingredients
        _directions.value = recipe.directions
        _imageUrl.value = recipe.imageUrl.orEmpty()
        _sourceUrl.value = recipe.sourceUrl.orEmpty()
        _servings.value = recipe.servings?.toString().orEmpty()
        _prepTime.value = recipe.prepTime?.toString().orEmpty()
        _cookTime.value = recipe.cookTime?.toString().orEmpty()
        _totalTime.value = recipe.totalTime?.toString().orEmpty()
        _calories.value = recipe.calories?.toString().orEmpty()
        _starRating.value = recipe.starRating
        _state.update { it.copy(isLoading = false, recipe = recipe) }
    }

    private fun shouldShowDiscardChangesDialog(
        originalRecipe: Recipe?,
        currentRecipe: Recipe,
    ): Boolean {
        return when {
            originalRecipe != null -> originalRecipe.isDirty()
            else -> currentRecipe.title.isNotBlank() ||
                    currentRecipe.description?.isNotBlank() == true ||
                    currentRecipe.ingredients.isNotBlank() ||
                    currentRecipe.directions.isNotBlank() ||
                    currentRecipe.imageUrl?.isNotBlank() == true ||
                    currentRecipe.sourceUrl?.isNotBlank() == true ||
                    currentRecipe.servings != null ||
                    currentRecipe.prepTime != null ||
                    currentRecipe.cookTime != null ||
                    currentRecipe.totalTime != null ||
                    currentRecipe.calories != null ||
                    currentRecipe.starRating != null
        }
    }

    private fun Recipe.isDirty(): Boolean =
        title != _title.value ||
                description.orEmpty() != _description.value ||
                ingredients != _ingredients.value ||
                directions != _directions.value ||
                imageUrl.orEmpty() != _imageUrl.value ||
                sourceUrl.orEmpty() != _sourceUrl.value ||
                servings?.toString().orEmpty() != _servings.value ||
                prepTime?.toString().orEmpty() != _prepTime.value ||
                cookTime?.toString().orEmpty() != _cookTime.value ||
                totalTime?.toString().orEmpty() != _totalTime.value ||
                calories?.toString().orEmpty() != _calories.value ||
                starRating != _starRating.value

    private fun currentRecipe(): Recipe = Recipe(
        id = -1L,
        title = _title.value,
        description = _description.value.ifBlank { null },
        ingredients = _ingredients.value,
        directions = _directions.value,
        imageUrl = _imageUrl.value.ifBlank { null },
        sourceUrl = _sourceUrl.value.ifBlank { null },
        servings = _servings.value.toIntOrNull(),
        prepTime = _prepTime.value.toIntOrNull(),
        cookTime = _cookTime.value.toIntOrNull(),
        totalTime = _totalTime.value.toIntOrNull(),
        calories = _calories.value.toIntOrNull(),
        starRating = _starRating.value,
        createdAt = Instant.DISTANT_PAST,
        updatedAt = Instant.DISTANT_PAST,
    )

    data class State(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val showDiscardChangesDialog: Boolean = false,
        val recipe: Recipe? = null,
    )

    sealed class Output {
        data object Cancelled : Output()
        data class Finished(
            val recipeId: Long,
        ) : Output()
    }
}