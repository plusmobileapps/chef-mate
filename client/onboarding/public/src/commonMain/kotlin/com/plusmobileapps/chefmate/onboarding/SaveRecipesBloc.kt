package com.plusmobileapps.chefmate.onboarding

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen

/**
 * Onboarding step explaining how to find recipes in the browser and download them to the account.
 */
interface SaveRecipesBloc : BlocScreen {
    fun onNextClicked()

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SaveRecipesBloc
    }
}
