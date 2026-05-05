package com.plusmobileapps.chefmate.cook.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.cook.WhatsCookingBloc
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class WhatsCookingViewModel(
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
    private val sessionRepository: CookingSessionRepository,
) : ViewModel(mainContext) {

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch {
            combine(sessionRepository.observeRecipeIds(), recipeRepository.getRecipes()) {
                    ids,
                    allRecipes ->
                    val byId = allRecipes.associateBy { it.id }
                    ids.mapNotNull { id ->
                        val recipe = byId[id] ?: return@mapNotNull null
                        WhatsCookingBloc.Model.Item(
                            recipeId = recipe.id,
                            title = recipe.title,
                            imageUrl = recipe.imageUrl,
                        )
                    }
                }
                .collect { items ->
                    _state.update { current ->
                        val visibleIds = items.map { it.recipeId }.toSet()
                        current.copy(
                            recipes = items,
                            selectedRecipeIds = current.selectedRecipeIds.intersect(visibleIds),
                        )
                    }
                }
        }
    }

    fun toggleSelectMode() {
        _state.update { current ->
            if (current.isSelectMode) {
                current.copy(isSelectMode = false, selectedRecipeIds = emptySet())
            } else {
                current.copy(isSelectMode = true)
            }
        }
    }

    fun toggleSelection(recipeId: Long) {
        _state.update { current ->
            val updated =
                if (recipeId in current.selectedRecipeIds) {
                    current.selectedRecipeIds - recipeId
                } else {
                    current.selectedRecipeIds + recipeId
                }
            current.copy(selectedRecipeIds = updated)
        }
    }

    fun deleteSelected() {
        val selected = _state.value.selectedRecipeIds.toList()
        if (selected.isEmpty()) return
        scope.launch { sessionRepository.stop(selected) }
        _state.update { it.copy(isSelectMode = false, selectedRecipeIds = emptySet()) }
    }

    data class State(
        val recipes: List<WhatsCookingBloc.Model.Item> = emptyList(),
        val isSelectMode: Boolean = false,
        val selectedRecipeIds: Set<Long> = emptySet(),
    )
}
