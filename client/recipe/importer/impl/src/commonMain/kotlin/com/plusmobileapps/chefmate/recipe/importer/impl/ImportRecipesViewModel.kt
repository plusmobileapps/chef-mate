@file:OptIn(ExperimentalTime::class)

package com.plusmobileapps.chefmate.recipe.importer.impl

import chefmate.client.recipe.importer.`public`.generated.resources.Res
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_item_subtitle
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_none_found
import chefmate.client.recipe.importer.`public`.generated.resources.import_recipes_parse_failed
import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipePhotoStorage
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.ImportItem
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Model
import com.plusmobileapps.chefmate.recipe.importer.ImportRecipesBloc.Phase
import com.plusmobileapps.chefmate.recipebook.data.RecipeBook
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class ImportRecipesViewModel(
    @Main mainContext: CoroutineContext,
    @IO private val ioContext: CoroutineContext,
    private val repository: RecipeRepository,
    private val recipeBookRepository: RecipeBookRepository,
    private val photoStorage: RecipePhotoStorage,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(Model())
    val state: StateFlow<Model> = _state.asStateFlow()

    val recipeBooks: StateFlow<List<RecipeBook>> =
        recipeBookRepository
            .getRecipeBooks()
            .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = emptyList())

    private val _selectedBookIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedBookIds: StateFlow<Set<Long>> = _selectedBookIds.asStateFlow()

    // Parsed recipes indexed by [ImportItem.id] (the item's position in the parsed list).
    private var parsed: List<ParsedArchiveRecipe> = emptyList()

    init {
        // Default the import target to the active book (falling back to the default book), matching
        // where a manually created recipe would land.
        scope.launch {
            val activeBook =
                recipeBookRepository.activeBookId.value ?: recipeBookRepository.getDefaultBookId()
            _selectedBookIds.update { it.ifEmpty { setOf(activeBook) } }
        }
    }

    fun onArchiveSelected(bytes: ByteArray, fileName: String) {
        _state.value = Model(Phase.Parsing)
        scope.launch {
            val recipes =
                try {
                    withContext(ioContext) { RecipeArchiveParser.parse(bytes) }
                } catch (t: Throwable) {
                    Logger.e(throwable = t, tag = TAG) { "Failed to parse archive $fileName" }
                    _state.value =
                        Model(Phase.Error(Res.string.import_recipes_parse_failed.asTextData()))
                    return@launch
                }
            if (recipes.isEmpty()) {
                _state.value = Model(Phase.Error(Res.string.import_recipes_none_found.asTextData()))
                return@launch
            }
            parsed = recipes
            val items =
                recipes
                    .mapIndexed { index, recipe ->
                        recipe.toItem(index.toString(), selected = true)
                    }
                    .toImmutableList()
            _state.value = Model(Phase.Review(items))
        }
    }

    fun onRecipeToggled(id: String) {
        updateReview { review ->
            review.copy(
                recipes =
                    review.recipes
                        .map { if (it.id == id) it.copy(selected = !it.selected) else it }
                        .toImmutableList()
            )
        }
    }

    fun onToggleSelectAll() {
        updateReview { review ->
            val selectAll = review.recipes.any { !it.selected }
            review.copy(
                recipes = review.recipes.map { it.copy(selected = selectAll) }.toImmutableList()
            )
        }
    }

    fun onToggleBook(bookId: Long) {
        _selectedBookIds.update { current ->
            if (bookId in current) current - bookId else current + bookId
        }
    }

    fun onImportClicked() {
        val review = _state.value.phase as? Phase.Review ?: return
        if (review.isImporting) return
        val selected = review.recipes.filter { it.selected }
        if (selected.isEmpty()) return

        val bookIds = _selectedBookIds.value
        _state.value = Model(review.copy(isImporting = true))
        scope.launch {
            try {
                var imported = 0
                for (item in selected) {
                    val recipe = parsed.getOrNull(item.id.toIntOrNull() ?: -1) ?: continue
                    repository.createRecipe(recipe.toRecipe().copy(recipeBookIds = bookIds))
                    imported++
                }
                _state.value = Model(Phase.Done(importedCount = imported))
            } catch (t: Throwable) {
                Logger.e(throwable = t, tag = TAG) { "Failed to import recipes" }
                _state.value =
                    Model(Phase.Error(Res.string.import_recipes_parse_failed.asTextData()))
            }
        }
    }

    fun onStartOver() {
        parsed = emptyList()
        _state.value = Model(Phase.Empty)
    }

    private inline fun updateReview(transform: (Phase.Review) -> Phase.Review) {
        _state.update { model ->
            val review = model.phase as? Phase.Review ?: return@update model
            model.copy(phase = transform(review))
        }
    }

    private suspend fun ParsedArchiveRecipe.toRecipe(): Recipe {
        val imageUrl = imageBytes?.let { bytes ->
            try {
                photoStorage.uploadPhoto(bytes = bytes, fileExtension = imageExtension ?: "jpg")
            } catch (t: Throwable) {
                Logger.e(throwable = t, tag = TAG) { "Failed to upload image for $title" }
                null
            }
        }
        return Recipe(
            id = -1,
            title = title,
            description = description,
            ingredients = ingredients.joinToString("\n"),
            directions = directions.joinToString("\n"),
            imageUrl = imageUrl,
            sourceUrl = sourceUrl,
            servings = servings,
            prepTime = prepTime,
            cookTime = cookTime,
            totalTime = totalTime,
            calories = calories,
            starRating = starRating?.takeIf { it > 0 },
            createdAt = Instant.DISTANT_PAST,
            updatedAt = Instant.DISTANT_PAST,
        )
    }

    private fun ParsedArchiveRecipe.toItem(id: String, selected: Boolean): ImportItem =
        ImportItem(
            id = id,
            title = title,
            subtitle =
                PhraseModel(
                    resource = Res.string.import_recipes_item_subtitle,
                    "ingredients" to FixedString(ingredients.size.toString()),
                    "steps" to FixedString(directions.size.toString()),
                ),
            hasImage = hasImage,
            selected = selected,
        )

    private companion object {
        const val TAG = "ImportRecipesViewModel"
    }
}
