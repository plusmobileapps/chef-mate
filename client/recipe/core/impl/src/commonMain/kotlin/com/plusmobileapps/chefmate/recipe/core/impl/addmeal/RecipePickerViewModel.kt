package com.plusmobileapps.chefmate.recipe.core.impl.addmeal

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.core.addmeal.RecipePickerBloc
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.plusmobileapps.chefmate.recipebook.data.RecipeBookRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class RecipePickerViewModel(
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
    private val recipeBookRepository: RecipeBookRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var allRecipes: List<RecipePickerBloc.RecipePickerItem> = emptyList()

    init {
        loadRecipes()
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query, displayRecipes = filter(allRecipes, query)) }
    }

    private fun loadRecipes() {
        scope.launch {
            combine(recipeRepository.getRecipes(), recipeBookRepository.getRecipeBooks()) {
                    recipes,
                    books ->
                    val bookNamesById = books.associate { it.id to it.name }
                    recipes.map { recipe ->
                        RecipePickerBloc.RecipePickerItem(
                            id = recipe.id,
                            title = recipe.title,
                            imageUrl = recipe.imageUrl,
                            // Surface which book(s) each result lives in so cross-book matches
                            // (e.g. same title in different books) are distinguishable.
                            bookNames =
                                recipe.recipeBookIds
                                    .mapNotNull { bookNamesById[it] }
                                    .sorted()
                                    .toImmutableList(),
                        )
                    }
                }
                .collect { items ->
                    allRecipes = items
                    _state.update {
                        it.copy(isLoading = false, displayRecipes = filter(items, it.searchQuery))
                    }
                }
        }
    }

    private fun filter(
        recipes: List<RecipePickerBloc.RecipePickerItem>,
        query: String,
    ): List<RecipePickerBloc.RecipePickerItem> {
        val trimmed = query.trim()
        return if (trimmed.isEmpty()) {
            recipes
        } else {
            recipes.filter { it.title.contains(trimmed, ignoreCase = true) }
        }
    }

    data class State(
        val isLoading: Boolean = true,
        val searchQuery: String = "",
        val displayRecipes: List<RecipePickerBloc.RecipePickerItem> = emptyList(),
    )
}
