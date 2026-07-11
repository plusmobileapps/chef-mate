package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Onboarding step explaining how to save a recipe from the user's own browser: on mobile by sharing
 * the page to ChefMate via the native share sheet, and on desktop by copying the page's URL from
 * the address bar and pasting it in. The screen picks the right guidance from the running platform.
 */
interface ShareRecipesBloc : ComposeScreen {
    fun onNextClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        ShareRecipesScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): ShareRecipesBloc
    }
}
