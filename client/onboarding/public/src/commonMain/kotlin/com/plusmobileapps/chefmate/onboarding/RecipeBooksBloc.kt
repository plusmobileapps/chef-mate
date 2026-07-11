package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Onboarding step introducing recipe books: grouping recipes into as many books as you like and
 * sharing a book with friends and family.
 */
interface RecipeBooksBloc : ComposeScreen {
    fun onNextClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        RecipeBooksScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): RecipeBooksBloc
    }
}
