package com.plusmobileapps.chefmate.recipe.bottomnav

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow

interface BottomNavOrderBloc {
    val state: StateFlow<Model>

    fun onMove(from: Int, to: Int)

    fun onSave()

    fun onBack()

    data class Model(
        val editedOrder: List<BottomNavBloc.Tab> = DEFAULT_TAB_ORDER,
        val persistedOrder: List<BottomNavBloc.Tab> = DEFAULT_TAB_ORDER,
    ) {
        val hasUnsavedChanges: Boolean
            get() = editedOrder != persistedOrder
    }

    sealed class Output {
        data object Back : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): BottomNavOrderBloc
    }
}
