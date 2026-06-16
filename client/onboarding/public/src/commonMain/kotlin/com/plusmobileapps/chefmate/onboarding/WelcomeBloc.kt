package com.plusmobileapps.chefmate.onboarding

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.BlocScreen

/** First screen of the onboarding flow — greets the user and kicks off the flow. */
interface WelcomeBloc : BlocScreen {
    fun onGetStartedClicked()

    fun onSignInClicked()

    fun onSkipClicked()

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object GetStarted : Output()

        /** The user already has an account and wants to sign in. */
        data object SignIn : Output()

        /** The user wants to skip onboarding entirely. */
        data object Skip : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): WelcomeBloc
    }
}
