package com.plusmobileapps.chefmate.recipe.core.impl.addgrocery

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.recipe.core.addgrocery.AddRecipeToGroceryListBloc.Model.ListItem
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

@Inject
class AddRecipeToGroceryListViewModel(
    @Assisted private val recipeId: Long,
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
    private val groceryRepository: GroceryRepository,
) : ViewModel(mainContext) {
    private val _output = Channel<Output>(Channel.BUFFERED)
    val output: Flow<Output> = _output.receiveAsFlow()
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        loadRecipe()
    }

    override fun onCleared() {
        super.onCleared()
        _output.close()
    }

    fun toggleIngredient(ingredientId: Int) {
        _state.update {
            it.copy(
                ingredients =
                    it.ingredients.map { ingredient ->
                        if (ingredient.id == ingredientId) {
                            ingredient.copy(isSelected = !ingredient.isSelected)
                        } else {
                            ingredient
                        }
                    },
            )
        }
    }

    fun save() {
        val ingredients =
            state.value.ingredients
                .filter { it.isSelected }
                .map { it.name }
        _state.update { it.copy(isAdding = true) }
        scope.launch {
            if (ingredients.isNotEmpty()) {
                groceryRepository.addGroceries(ingredients)
            }
            _output.send(Output.Finished)
        }
    }

    private fun loadRecipe() {
        scope.launch {
            val recipe =
                recipeRepository.getRecipe(recipeId).first() ?: run {
                    _output.send(Output.Finished)
                    return@launch
                }
            val ingredients =
                recipe.ingredients
                    .split("\n")
                    .mapIndexedNotNull { index, ingredient ->
                        if (ingredient.isBlank()) {
                            null
                        } else {
                            ListItem(
                                id = ingredient.hashCode(),
                                name = ingredient,
                                isSelected = true,
                            )
                        }
                    }
            _state.update {
                it.copy(
                    isLoading = false,
                    recipe = recipe,
                    ingredients = ingredients,
                )
            }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val isAdding: Boolean = false,
        val recipe: Recipe = Recipe.Empty,
        val ingredients: List<ListItem> = emptyList(),
    )

    sealed class Output {
        data object Finished : Output()
    }
}
