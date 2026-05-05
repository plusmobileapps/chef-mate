package com.plusmobileapps.chefmate.recipe.list.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipe.list.RecipeFilterOption
import com.plusmobileapps.chefmate.recipe.list.RecipeSortOption
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.string
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
    private val cookingSessionRepository: CookingSessionRepository,
    settings: Settings,
) : ViewModel(mainContext) {
    private var isGridViewPref by settings.boolean(KEY_IS_GRID_VIEW, false)
    private var sortOptionPref by
        settings.string(KEY_SORT_OPTION, RecipeSortOption.RECENTLY_ADDED.name)
    private var activeFiltersPref by settings.string(KEY_ACTIVE_FILTERS, "")

    private val _state =
        MutableStateFlow(
            State(
                isGridView = isGridViewPref,
                currentSort =
                    RecipeSortOption.entries.find { it.name == sortOptionPref }
                        ?: RecipeSortOption.RECENTLY_ADDED,
                activeFilters =
                    activeFiltersPref
                        .split(",")
                        .filter { it.isNotBlank() }
                        .mapNotNull { name -> RecipeFilterOption.entries.find { it.name == name } }
                        .toSet(),
            )
        )
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch { observeRecipes() }
        scope.launch { observeCookingSession() }
    }

    private suspend fun observeRecipes() {
        repository.getRecipes().collect { recipes ->
            _state.update { it.copy(isLoading = false, recipes = recipes) }
        }
    }

    private suspend fun observeCookingSession() {
        cookingSessionRepository.observeRecipeIds().collect { ids ->
            _state.update { it.copy(cookingRecipeIds = ids) }
        }
    }

    fun showDoneCookingDialog() {
        _state.update { it.copy(showDoneCookingDialog = true) }
    }

    fun dismissDoneCookingDialog() {
        _state.update { it.copy(showDoneCookingDialog = false) }
    }

    fun confirmDoneCooking() {
        _state.update { it.copy(showDoneCookingDialog = false) }
        scope.launch { cookingSessionRepository.stopAll() }
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
        sortOptionPref = option.name
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
        persistFilters()
    }

    fun clearFilters() {
        _state.update { it.copy(activeFilters = emptySet()) }
        persistFilters()
    }

    fun applySortAndFilters(sort: RecipeSortOption, filters: Set<RecipeFilterOption>) {
        _state.update { it.copy(currentSort = sort, activeFilters = filters) }
        sortOptionPref = sort.name
        persistFilters()
    }

    private fun persistFilters() {
        activeFiltersPref = _state.value.activeFilters.joinToString(",") { it.name }
    }

    fun toggleViewMode() {
        _state.update { it.copy(isGridView = !it.isGridView) }
        isGridViewPref = _state.value.isGridView
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onSyncClicked() {
        scope.launch {
            _state.update { it.copy(isSyncing = true) }
            try {
                repository.syncAllUnsynced()
            } finally {
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val isSyncing: Boolean = false,
        val recipes: List<Recipe> = emptyList(),
        val currentSort: RecipeSortOption = RecipeSortOption.RECENTLY_ADDED,
        val activeFilters: Set<RecipeFilterOption> = emptySet(),
        val isGridView: Boolean = false,
        val searchQuery: String = "",
        val cookingRecipeIds: List<Long> = emptyList(),
        val showDoneCookingDialog: Boolean = false,
    ) {
        val isSearchActive: Boolean
            get() = searchQuery.isNotBlank()

        val displayRecipes: List<Recipe>
            get() =
                recipes
                    .let { applySearch(it, searchQuery) }
                    .let { applyFilters(it, activeFilters) }
                    .let { applySort(it, currentSort) }
    }
}

private const val KEY_IS_GRID_VIEW = "recipe_list_is_grid_view"
private const val KEY_SORT_OPTION = "recipe_list_sort_option"
private const val KEY_ACTIVE_FILTERS = "recipe_list_active_filters"

private fun applySearch(recipes: List<Recipe>, query: String): List<Recipe> {
    if (query.isBlank()) return recipes
    val lowerQuery = query.lowercase()
    return recipes.filter { recipe ->
        recipe.title.lowercase().contains(lowerQuery) ||
            recipe.description?.lowercase()?.contains(lowerQuery) == true
    }
}

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
