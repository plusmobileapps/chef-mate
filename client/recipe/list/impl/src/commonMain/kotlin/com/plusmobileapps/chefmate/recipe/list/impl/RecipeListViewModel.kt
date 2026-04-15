package com.plusmobileapps.chefmate.recipe.list.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class RecipeListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
    settings: Settings,
) : ViewModel(mainContext) {
    private var isGridViewPref by settings.boolean(KEY_IS_GRID_VIEW, false)

    private val _state = MutableStateFlow(State(isGridView = isGridViewPref))
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch { observeRecipes() }
    }

    private suspend fun observeRecipes() {
        repository.getRecipes().collect { recipes ->
            _state.update { it.copy(isLoading = false, recipes = recipes) }
        }
    }

    fun deleteRecipe(recipeId: Long) {
        scope.launch { repository.deleteRecipe(recipeId) }
    }

    fun toggleFavorite(recipeId: Long) {
        scope.launch {
            val recipe = repository.getRecipe(recipeId).first() ?: return@launch
            repository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
        }
    }

    fun updateSort(option: RecipeSortOption) {
        _state.update { it.copy(currentSort = option) }
    }

    fun toggleFilter(filter: RecipeFilterOption) {
        _state.update { state ->
            val newFilters =
                if (filter in state.activeFilters) {
                    state.activeFilters - filter
                } else {
                    state.activeFilters + filter
                }
            state.copy(activeFilters = newFilters)
        }
    }

    fun toggleViewMode() {
        _state.update { it.copy(isGridView = !it.isGridView) }
        isGridViewPref = _state.value.isGridView
    }

    data class State(
        val isLoading: Boolean = true,
        val recipes: List<Recipe> = emptyList(),
        val currentSort: RecipeSortOption = RecipeSortOption.RECENTLY_ADDED,
        val activeFilters: Set<RecipeFilterOption> = emptySet(),
        val isGridView: Boolean = false,
    ) {
        val displayRecipes: List<Recipe>
            get() =
                recipes.let { applyFilters(it, activeFilters) }.let { applySort(it, currentSort) }
    }
}

private const val KEY_IS_GRID_VIEW = "recipe_list_is_grid_view"

private fun applyFilters(recipes: List<Recipe>, filters: Set<RecipeFilterOption>): List<Recipe> {
    if (filters.isEmpty()) return recipes
    return recipes.filter { recipe ->
        filters.all { filter ->
            when (filter) {
                RecipeFilterOption.FAVORITES -> recipe.isFavorite
                RecipeFilterOption.RATED -> recipe.starRating != null
                RecipeFilterOption.QUICK_RECIPES -> (recipe.totalTime ?: Int.MAX_VALUE) <= 30
            }
        }
    }
}

private fun applySort(recipes: List<Recipe>, sort: RecipeSortOption): List<Recipe> =
    when (sort) {
        RecipeSortOption.RECENTLY_ADDED -> recipes.sortedByDescending { it.createdAt }
        RecipeSortOption.OLDEST_FIRST -> recipes.sortedBy { it.createdAt }
        RecipeSortOption.ALPHABETICAL_ASC -> recipes.sortedBy { it.title.lowercase() }
        RecipeSortOption.ALPHABETICAL_DESC -> recipes.sortedByDescending { it.title.lowercase() }
        RecipeSortOption.TOP_RATED ->
            recipes.sortedWith(compareByDescending<Recipe, Int?>(nullsLast()) { it.starRating })
    }
