package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc.Tab.RECIPES
import com.plusmobileapps.chefmate.recipe.bottomnav.TabOrderPreferences
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@Inject
class BottomNavViewModel(
    @Main mainContext: CoroutineContext,
    tabOrderPreferences: TabOrderPreferences,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State(tabs = tabOrderPreferences.tabOrder.value))
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        tabOrderPreferences.tabOrder
            .onEach { latest -> _state.update { it.copy(tabs = latest) } }
            .launchIn(scope)
    }

    fun selectTab(tab: BottomNavBloc.Tab) {
        _state.value = _state.value.copy(selectedTab = tab)
    }

    data class State(
        val selectedTab: BottomNavBloc.Tab = RECIPES,
        val tabs: List<BottomNavBloc.Tab>,
    )
}
