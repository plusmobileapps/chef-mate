package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavBloc
import com.plusmobileapps.chefmate.recipe.bottomnav.DEFAULT_TAB_ORDER
import com.plusmobileapps.chefmate.recipe.bottomnav.TabOrderPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTabOrderPreferences(initial: List<BottomNavBloc.Tab> = DEFAULT_TAB_ORDER) :
    TabOrderPreferences {
    private val _tabOrder = MutableStateFlow(initial)
    override val tabOrder: StateFlow<List<BottomNavBloc.Tab>> = _tabOrder.asStateFlow()

    override fun setTabOrder(tabs: List<BottomNavBloc.Tab>) {
        _tabOrder.value = tabs
    }
}
