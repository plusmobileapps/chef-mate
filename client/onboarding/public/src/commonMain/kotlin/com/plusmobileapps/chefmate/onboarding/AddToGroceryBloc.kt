package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Onboarding step calling out the recipe detail toolbar's "add to grocery list" action, which sends
 * a recipe's ingredients straight to a grocery list.
 */
interface AddToGroceryBloc : ComposeScreen {
    fun onNextClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        AddToGroceryScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): AddToGroceryBloc
    }
}
