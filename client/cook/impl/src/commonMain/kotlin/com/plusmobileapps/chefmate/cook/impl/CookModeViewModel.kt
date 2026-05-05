package com.plusmobileapps.chefmate.cook.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.data.Recipe
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@AssistedInject
class CookModeViewModel(
    @Assisted private val initialRecipeId: Long,
    @Main mainContext: CoroutineContext,
    private val recipeRepository: RecipeRepository,
    private val sessionRepository: CookingSessionRepository,
    settings: Settings,
) : ViewModel(mainContext) {

    private var splitLayoutPref by settings.boolean(KEY_LAYOUT_SPLIT, defaultValue = false)

    private val _state =
        MutableStateFlow(
            State(
                layoutMode =
                    if (splitLayoutPref) CookModeBloc.LayoutMode.Split
                    else CookModeBloc.LayoutMode.Stacked
            )
        )
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        scope.launch {
            sessionRepository.start(initialRecipeId)
            sessionRepository.markSelected(initialRecipeId)
        }
        scope.launch {
            combine(sessionRepository.observeRecipeIds(), recipeRepository.getRecipes()) {
                    ids,
                    allRecipes ->
                    val byId = allRecipes.associateBy { it.id }
                    ids.mapNotNull { byId[it] }
                }
                .collect { recipes ->
                    // Recipes arrive ordered by lastSelectedAt DESC, so the first is active.
                    _state.update { current ->
                        current.copy(
                            isLoading = false,
                            cookingRecipes = recipes,
                            activeRecipeId = recipes.firstOrNull()?.id,
                        )
                    }
                }
        }
    }

    fun selectRecipe(recipeId: Long) {
        scope.launch { sessionRepository.markSelected(recipeId) }
    }

    fun toggleLayout() {
        val next =
            if (_state.value.layoutMode == CookModeBloc.LayoutMode.Stacked) {
                CookModeBloc.LayoutMode.Split
            } else {
                CookModeBloc.LayoutMode.Stacked
            }
        splitLayoutPref = next == CookModeBloc.LayoutMode.Split
        _state.update { it.copy(layoutMode = next) }
    }

    data class State(
        val isLoading: Boolean = true,
        val cookingRecipes: List<Recipe> = emptyList(),
        val activeRecipeId: Long? = null,
        val layoutMode: CookModeBloc.LayoutMode = CookModeBloc.LayoutMode.Stacked,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(initialRecipeId: Long): CookModeViewModel
    }

    private companion object {
        const val KEY_LAYOUT_SPLIT = "cook_mode_layout_split"
    }
}
