package com.plusmobileapps.chefmate.cook.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.combineStates
import com.plusmobileapps.chefmate.cook.CookModeBloc
import com.plusmobileapps.chefmate.cook.data.CookingSessionRepository
import com.plusmobileapps.chefmate.di.CoachMarkController
import com.plusmobileapps.chefmate.di.CoachMarkId
import com.plusmobileapps.chefmate.di.KeepScreenOnRepository
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
    private val keepScreenOnRepository: KeepScreenOnRepository,
    private val coachMarkController: CoachMarkController,
) : ViewModel(mainContext) {

    // Split view is the default; users can toggle to the scrolling (Stacked) list, which persists.
    private var splitLayoutPref by settings.boolean(KEY_LAYOUT_SPLIT, defaultValue = true)

    private val _state =
        MutableStateFlow(
            State(
                layoutMode =
                    if (splitLayoutPref) CookModeBloc.LayoutMode.Split
                    else CookModeBloc.LayoutMode.Stacked,
                keepScreenOn = keepScreenOnRepository.keepScreenOn.value,
            )
        )

    // Fold the shared controller's active mark into state so the screen shows at most one coach
    // mark at a time across the whole app.
    val state: StateFlow<State> =
        combineStates(_state, coachMarkController.activeCoachMark) { state, activeCoachMark ->
            state.copy(activeCoachMark = activeCoachMark)
        }

    init {
        // Queue every cook-mode coach mark in order; the controller shows them one at a time.
        CoachMarkId.cookModeSequence.forEach(coachMarkController::request)
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
        scope.launch {
            keepScreenOnRepository.keepScreenOn.collect { keepScreenOn ->
                _state.update { it.copy(keepScreenOn = keepScreenOn) }
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

    fun toggleKeepScreenOn() {
        keepScreenOnRepository.toggle()
    }

    /**
     * Mark a coach mark seen, but only if it's the one currently showing, so tapping a control
     * dismisses its own tip without consuming tips further down the queue.
     */
    fun dismissCoachMark(id: String) {
        if (coachMarkController.activeCoachMark.value == id) {
            coachMarkController.dismiss(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Leaving the screen without dismissing frees the queue so other coach marks can show.
        CoachMarkId.cookModeSequence.forEach(coachMarkController::release)
    }

    data class State(
        val isLoading: Boolean = true,
        val cookingRecipes: List<Recipe> = emptyList(),
        val activeRecipeId: Long? = null,
        val layoutMode: CookModeBloc.LayoutMode = CookModeBloc.LayoutMode.Split,
        val keepScreenOn: Boolean = true,
        val activeCoachMark: String? = null,
    )

    @AssistedFactory
    fun interface Factory {
        fun create(initialRecipeId: Long): CookModeViewModel
    }

    private companion object {
        const val KEY_LAYOUT_SPLIT = "cook_mode_layout_split"
    }
}
