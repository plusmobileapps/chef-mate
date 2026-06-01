package com.plusmobileapps.chefmate.recipe.exporter.impl

import chefmate.client.recipe.exporter.`public`.generated.resources.Res
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_generate_failed
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_item_subtitle
import chefmate.client.recipe.exporter.`public`.generated.resources.export_recipes_save_cancelled
import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.IO
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.ExportItem
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Model
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.PendingSave
import com.plusmobileapps.chefmate.recipe.exporter.ExportRecipesBloc.Phase
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.asTextData
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Inject
class ExportRecipesViewModel(
    @Main mainContext: CoroutineContext,
    @IO private val ioContext: CoroutineContext,
    private val repository: RecipeRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(Model())
    val state: StateFlow<Model> = _state.asStateFlow()

    // Recipes loaded from the repository, keyed by [ExportItem.id] (recipe.id.toString()) so we
    // can look up the underlying [Recipe] when the user clicks Export.
    private var loadedRecipes: Map<String, Recipe> = emptyMap()

    // Increments on each generated archive so the screen's LaunchedEffect keys flip even when
    // the user re-exports the same selection.
    private var saveToken: Long = 0L

    init {
        loadRecipes()
    }

    private fun loadRecipes() {
        _state.value = Model(Phase.Loading)
        scope.launch {
            val recipes =
                try {
                    withContext(ioContext) { repository.getRecipes().first() }
                } catch (t: Throwable) {
                    Logger.e(throwable = t, tag = TAG) { "Failed to load recipes for export" }
                    _state.value =
                        Model(Phase.Error(Res.string.export_recipes_generate_failed.asTextData()))
                    return@launch
                }
            if (recipes.isEmpty()) {
                _state.value = Model(Phase.Empty)
                return@launch
            }
            loadedRecipes = recipes.associateBy { it.id.toString() }
            val items = recipes.map { it.toItem(selected = true) }
            _state.value = Model(Phase.Review(items))
        }
    }

    fun onRecipeToggled(id: String) {
        updateReview { review ->
            review.copy(
                recipes =
                    review.recipes.map { if (it.id == id) it.copy(selected = !it.selected) else it }
            )
        }
    }

    fun onToggleSelectAll() {
        updateReview { review ->
            val selectAll = review.recipes.any { !it.selected }
            review.copy(recipes = review.recipes.map { it.copy(selected = selectAll) })
        }
    }

    fun onExportClicked() {
        val review = _state.value.phase as? Phase.Review ?: return
        if (review.isExporting) return
        val selected =
            review.recipes.filter { it.selected }.mapNotNull { item -> loadedRecipes[item.id] }
        if (selected.isEmpty()) return

        _state.value = _state.value.copy(phase = review.copy(isExporting = true))
        scope.launch {
            val archive =
                try {
                    withContext(ioContext) { RecipeArchiveBuilder.build(selected) }
                } catch (t: Throwable) {
                    Logger.e(throwable = t, tag = TAG) { "Failed to build export archive" }
                    _state.value =
                        Model(Phase.Error(Res.string.export_recipes_generate_failed.asTextData()))
                    return@launch
                }
            saveToken += 1
            val fileName = exportFileName(count = selected.size)
            _state.value =
                Model(
                    phase = review.copy(isExporting = true),
                    pendingSave =
                        PendingSave(token = saveToken, fileName = fileName, archive = archive),
                )
        }
    }

    fun onSaveCompleted(saved: Boolean) {
        val previous = _state.value
        previous.pendingSave ?: return
        val review = previous.phase as? Phase.Review
        _state.value =
            if (saved) {
                val exportedCount = review?.recipes?.count { it.selected } ?: 0
                Model(phase = Phase.Done(exportedCount))
            } else {
                // Cancel vs. write-failure look the same from the user's seat — bounce them to
                // an error state with a "Try again" affordance that re-runs `loadRecipes()`.
                Model(Phase.Error(Res.string.export_recipes_save_cancelled.asTextData()))
            }
    }

    fun onStartOver() {
        loadRecipes()
    }

    private inline fun updateReview(transform: (Phase.Review) -> Phase.Review) {
        _state.update { model ->
            val review = model.phase as? Phase.Review ?: return@update model
            model.copy(phase = transform(review))
        }
    }

    private fun Recipe.toItem(selected: Boolean): ExportItem =
        ExportItem(
            id = id.toString(),
            title = title,
            subtitle =
                PhraseModel(
                    resource = Res.string.export_recipes_item_subtitle,
                    "ingredients" to FixedString(ingredients.lineCount().toString()),
                    "steps" to FixedString(directions.lineCount().toString()),
                ),
            hasImage = !imageUrl.isNullOrBlank(),
            selected = selected,
        )

    private fun String.lineCount(): Int =
        if (isBlank()) 0 else split('\n').count { it.isNotBlank() }

    private fun exportFileName(count: Int): String {
        val noun = if (count == 1) "recipe" else "recipes"
        return "chef-mate-$count-$noun.zip"
    }

    private companion object {
        const val TAG = "ExportRecipesViewModel"
    }
}
