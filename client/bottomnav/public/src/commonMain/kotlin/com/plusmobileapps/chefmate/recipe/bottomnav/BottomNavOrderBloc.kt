package com.plusmobileapps.chefmate.recipe.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.StateFlow

interface BottomNavOrderBloc : ComposeScreen {
    val state: StateFlow<Model>

    @Composable
    override fun Content(modifier: Modifier) {
        BottomNavOrderScreen(bloc = this, modifier = modifier)
    }

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
