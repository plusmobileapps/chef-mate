package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.GROCERIES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.RECIPES
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.SETTINGS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

@Inject
class BottomNavViewModel(
    @Main mainContext: CoroutineContext,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun selectTab(tab: BottomNavBloc.Tab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    data class State(
        val selectedTab: BottomNavBloc.Tab = RECIPES,
        val tabs: List<BottomNavBloc.Tab> = listOf(RECIPES, GROCERIES, SETTINGS),
    )
}
