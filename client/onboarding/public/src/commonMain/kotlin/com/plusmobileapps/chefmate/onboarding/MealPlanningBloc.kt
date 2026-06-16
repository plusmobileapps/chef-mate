package com.plusmobileapps.chefmate.onboarding

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen

/** Onboarding step explaining how to add recipes to meals across the days of the week. */
interface MealPlanningBloc : BlocScreen {
    fun onNextClicked()

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object Next : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): MealPlanningBloc
    }
}
