package com.plusmobileapps.chefmate.recipe.bottomnav

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow

interface BottomNavOrderBloc : BlocScreen {
    val state: StateFlow<Model>

    fun onMove(from: Int, to: Int)

    fun onSave()

    fun onBack()

    data class Model(
        val editedOrder: ImmutableList<BottomNavBloc.Tab> = DEFAULT_TAB_ORDER.toImmutableList(),
        val persistedOrder: ImmutableList<BottomNavBloc.Tab> = DEFAULT_TAB_ORDER.toImmutableList(),
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
