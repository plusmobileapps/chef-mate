package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Onboarding step explaining how to find recipes with the app's built-in browser and download them
 * directly to the account.
 */
interface SaveRecipesBloc : ComposeScreen {
    fun onNextClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        SaveRecipesScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SaveRecipesBloc
    }
}
