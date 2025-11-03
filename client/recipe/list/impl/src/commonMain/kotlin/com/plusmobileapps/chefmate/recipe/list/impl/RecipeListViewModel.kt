package com.plusmobileapps.chefmate.recipe.list.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

@Inject
class RecipeListViewModel(
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch { observeRecipes() }
    }

    private suspend fun observeRecipes() {
        repository.getRecipes().collect { recipes ->
            _state.update {
                it.copy(
                    isLoading = false,
                    recipes = recipes,
                )
            }
        }
    }

    fun deleteRecipe(recipeId: Long) {
        scope.launch {
            repository.deleteRecipe(recipeId)
        }
    }

    fun toggleFavorite(recipeId: Long) {
        scope.launch {
            val recipe = repository.getRecipe(recipeId).first() ?: return@launch
            repository.updateRecipe(recipe.copy(isFavorite = !recipe.isFavorite))
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val recipes: List<Recipe> = emptyList(),
    )
}
