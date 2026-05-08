package com.plusmobileapps.chefmate.recipe.bottomnav.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.recipe.bottomnav.BottomNavOrderBloc
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
class BottomNavOrderViewModel(
    @Main mainContext: CoroutineContext,
    private val tabOrderPreferences: TabOrderPreferences,
) : ViewModel(mainContext) {

    private val initialOrder = tabOrderPreferences.tabOrder.value

    private val _state =
        MutableStateFlow(
            BottomNavOrderBloc.Model(editedOrder = initialOrder, persistedOrder = initialOrder)
        )
    val state: StateFlow<BottomNavOrderBloc.Model> = _state.asStateFlow()

    init {
        // Keep `persistedOrder` in sync with the repository so external writes (e.g. the user adds
        // a new tab in a future build) update the "Save" affordance correctly.
        tabOrderPreferences.tabOrder
            .onEach { latest -> _state.update { it.copy(persistedOrder = latest) } }
            .launchIn(scope)
    }

    fun move(from: Int, to: Int) {
        _state.update { current ->
            val list = current.editedOrder
            if (from !in list.indices || to !in list.indices || from == to) return@update current
            val mutable = list.toMutableList()
            val item = mutable.removeAt(from)
            mutable.add(to, item)
            current.copy(editedOrder = mutable)
        }
    }

    fun save() {
        val order = _state.value.editedOrder
        tabOrderPreferences.setTabOrder(order)
    }
}
