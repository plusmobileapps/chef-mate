package com.plusmobileapps.chefmate.onboarding

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen

/** Final screen of the onboarding flow — confirms setup and launches the rest of the app. */
interface StartCookingBloc : BlocScreen {
    fun onStartCookingClicked()

    sealed class Output {
        /** The user is done with onboarding and wants to start using the app. */
        data object StartCooking : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): StartCookingBloc
    }
}
