@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.core.impl.edit

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.BuiltinCategory
import com.plusmobileapps.chefmate.recipe.data.Category
import com.plusmobileapps.chefmate.recipe.data.CategoryRepository
import com.plusmobileapps.chefmate.recipe.data.ExtractedRecipeData
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipePhotoStorage
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class EditRecipeViewModel(
    @Assisted private val recipeId: Long?,
    @Assisted extractedRecipe: ExtractedRecipeData?,
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
    private val categoryRepository: CategoryRepository,
    private val photoStorage: RecipePhotoStorage,
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

    private val _categories = MutableStateFlow<Set<Category>>(emptySet())
    val categories: StateFlow<Set<Category>> = _categories.asStateFlow()

    val availableUserCategories: StateFlow<List<Category>> =
        categoryRepository
            .observeUserCategories()
            .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = emptyList())

    // Re-entrancy guard for [createUserCategoryAndAttach]; not exposed to the bloc because the
    // picker UI doesn't need a loading flag (offline-first inserts are effectively instant, and
    // routing a fast true→false transition through StateFlow could be conflated and leave the
    // UI stuck on a spinner).
    private val _isCreatingCategory = MutableStateFlow(false)

    private val _pendingPhotoBytes = MutableStateFlow<ByteArray?>(null)
    val pendingPhotoBytes: StateFlow<ByteArray?> = _pendingPhotoBytes.asStateFlow()
    private var pendingPhotoExtension: String? = null

    init {
        when {
            recipeId != null -> {
                _state.update { it.copy(isLoading = false) }
                scope.launch { loadRecipe(recipeId) }
            }
            extractedRecipe != null -> {
                _title.value = extractedRecipe.title
                _description.value = extractedRecipe.description.orEmpty()
                _ingredients.value = extractedRecipe.ingredients.joinToString("\n")
                _directions.value = extractedRecipe.directions.joinToString("\n")
                _imageUrl.value = extractedRecipe.imageUrl.orEmpty()
                _sourceUrl.value = extractedRecipe.sourceUrl
                _servings.value = extractedRecipe.servings?.toString().orEmpty()
                _prepTime.value = extractedRecipe.prepTime?.toString().orEmpty()
                _cookTime.value = extractedRecipe.cookTime?.toString().orEmpty()
                _totalTime.value = extractedRecipe.totalTime?.toString().orEmpty()
                _calories.value = extractedRecipe.calories?.toString().orEmpty()
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

    fun updateCategories(value: Set<Category>) {
        _categories.value = value
    }

    fun attachBuiltin(builtin: BuiltinCategory) {
        scope.launch {
            val materialized = categoryRepository.materializeBuiltin(builtin)
            _categories.update { it.upsertById(materialized) }
        }
    }

    fun attachCategory(category: Category) {
        _categories.update { it.upsertById(category) }
    }

    fun detachCategory(category: Category) {
        _categories.update { current -> current.filterNot { it.id == category.id }.toSet() }
    }

    /** Renames a user-created category in place. The local DB write is offline-first. */
    fun renameUserCategory(id: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        scope.launch {
            val renamed = categoryRepository.renameCategory(id, trimmed)
            _categories.update { current ->
                if (current.any { it.id == id }) current.upsertById(renamed) else current
            }
        }
    }

    /**
     * Deletes a user-created category. If the category was attached to this recipe, it's removed
     * from the in-memory selection immediately so the chip disappears on save. Other recipes that
     * referenced it lose their attachment via Supabase's ON DELETE CASCADE on recipe_categories.
     */
    fun deleteUserCategory(id: Long) {
        scope.launch {
            _categories.update { current -> current.filterNot { it.id == id }.toSet() }
            categoryRepository.deleteCategory(id)
        }
    }

    /**
     * Creates a new user category and immediately attaches it to the recipe being edited. The local
     * DB insert is synchronous (offline-first); the remote push is fire-and-forget inside the repo,
     * so the new row appears in [availableUserCategories] right away and the picker dismisses its
     * inline create field immediately on submit.
     */
    fun createUserCategoryAndAttach(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank() || _isCreatingCategory.value) return
        scope.launch {
            _isCreatingCategory.value = true
            try {
                val created = categoryRepository.createUserCategory(trimmed)
                _categories.update { it.upsertById(created) }
            } finally {
                _isCreatingCategory.value = false
            }
        }
    }

    /**
     * Adds [category] to the set, replacing any existing entry with the same id. Set's structural
     * equality treats two Category snapshots with different `syncStatus` as distinct, which would
     * let the same row appear twice when the background remote push mutates the underlying DB row
     * between two attaches.
     */
    private fun Set<Category>.upsertById(category: Category): Set<Category> =
        filterNot { it.id == category.id }.toSet() + category

    fun tryToClose() {
        val originalRecipe = _state.value.recipe
        val currentRecipe = currentRecipe()
        when {
            shouldShowDiscardChangesDialog(originalRecipe, currentRecipe) -> {
                _state.update { it.copy(showDiscardChangesDialog = true) }
            }
            else -> {
                scope.launch { _output.send(Output.Cancelled) }
            }
        }
    }

    fun dismissDiscardChangesDialog() {
        _state.update { it.copy(showDiscardChangesDialog = false) }
    }

    fun setPendingPhoto(bytes: ByteArray, fileExtension: String) {
        _pendingPhotoBytes.value = bytes
        pendingPhotoExtension = fileExtension
        _state.update { it.copy(uploadError = null) }
    }

    fun dismissUploadError() {
        _state.update { it.copy(uploadError = null) }
    }

    fun save() {
        if (_state.value.isSaving) return
        _state.update { it.copy(isSaving = true, uploadError = null) }
        scope.launch {
            val pendingBytes = _pendingPhotoBytes.value
            val pendingExt = pendingPhotoExtension
            if (pendingBytes != null && pendingExt != null) {
                try {
                    val url =
                        photoStorage.uploadPhoto(bytes = pendingBytes, fileExtension = pendingExt)
                    _imageUrl.value = url
                    _pendingPhotoBytes.value = null
                    pendingPhotoExtension = null
                } catch (t: Throwable) {
                    Logger.e(throwable = t, tag = "EditRecipeViewModel") {
                        "Failed to upload photo"
                    }
                    _state.update { it.copy(isSaving = false, uploadError = t) }
                    return@launch
                }
            }
            val originalRecipe = _state.value.recipe
            val currentRecipe = currentRecipe()
            val savedRecipe =
                if (originalRecipe != null) {
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
        _categories.value = recipe.categories
        _state.update { it.copy(isLoading = false, recipe = recipe) }
    }

    private fun shouldShowDiscardChangesDialog(
        originalRecipe: Recipe?,
        currentRecipe: Recipe,
    ): Boolean {
        if (_pendingPhotoBytes.value != null) return true
        return when {
            originalRecipe != null -> originalRecipe.isDirty()
            else ->
                currentRecipe.title.isNotBlank() ||
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
                    currentRecipe.starRating != null ||
                    currentRecipe.categories.isNotEmpty()
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
            starRating != _starRating.value ||
            categories != _categories.value

    private fun currentRecipe(): Recipe =
        Recipe(
            id = recipeId ?: -1,
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
            isFavorite = state.value.recipe?.isFavorite ?: false,
            categories = _categories.value,
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )

    data class State(
        val isLoading: Boolean = false,
        val isSaving: Boolean = false,
        val showDiscardChangesDialog: Boolean = false,
        val recipe: Recipe? = null,
        val uploadError: Throwable? = null,
    )

    sealed class Output {
        data object Cancelled : Output()

        data class Finished(val recipeId: Long) : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(recipeId: Long?, extractedRecipe: ExtractedRecipeData?): EditRecipeViewModel
    }
}
