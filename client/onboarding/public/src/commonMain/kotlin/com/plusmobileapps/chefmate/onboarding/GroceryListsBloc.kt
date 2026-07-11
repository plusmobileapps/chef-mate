package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Onboarding step introducing grocery lists: keeping more than one list and collaborating on a
 * shared list with friends in real time.
 */
interface GroceryListsBloc : ComposeScreen {
    fun onNextClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        GroceryListsScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): GroceryListsBloc
    }
}
