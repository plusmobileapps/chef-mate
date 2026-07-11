package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Onboarding step calling out the recipe detail toolbar's "start cooking" action, which opens the
 * hands-free, step-by-step Cook Mode view.
 */
interface CookModeBloc : ComposeScreen {
    fun onNextClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        CookModeScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): CookModeBloc
    }
}
