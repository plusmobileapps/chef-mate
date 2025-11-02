package com.plusmobileapps.chefmate.recipe.core.impl.detail

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

@Inject
class RecipeDetailViewModel(
    @Assisted private val recipeId: Long,
    @Main mainContext: CoroutineContext,
    private val repository: RecipeRepository,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch { observeRecipe() }
    }

    private suspend fun observeRecipe() {
        repository.getRecipe(recipeId).collect { recipe ->
            _state.update {
                it.copy(
                    isLoading = false,
                    recipe = recipe ?: Recipe.Empty,
                )
            }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val recipe: Recipe = Recipe.Empty,
    )
}