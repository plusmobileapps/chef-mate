package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.core.addmeal.RecipePickerBloc
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class RecipePickerViewModel(
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var allRecipes: List<RecipePickerBloc.RecipePickerItem> = emptyList()

    init {
        loadRecipes()
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applySearch()
    }

    private fun loadRecipes() {
        scope.launch {
            recipeRepository.getRecipes().collect { recipes ->
                allRecipes = recipes.map { recipe ->
                    RecipePickerBloc.RecipePickerItem(
                        id = recipe.id,
                        title = recipe.title,
                        imageUrl = recipe.imageUrl,
                    )
                }
                _state.update { it.copy(isLoading = false) }
                applySearch()
            }
        }
    }

    private fun applySearch() {
        val query = _state.value.searchQuery.trim()
        val filtered =
            if (query.isEmpty()) {
                allRecipes
            } else {
                allRecipes.filter { it.title.contains(query, ignoreCase = true) }
            }
        _state.update { it.copy(displayRecipes = filtered) }
    }

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String = "",
        val displayRecipes: List<RecipePickerBloc.RecipePickerItem> = emptyList(),
    )
}
