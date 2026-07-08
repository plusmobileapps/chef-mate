package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/** First screen of the onboarding flow — greets the user and kicks off the flow. */
interface WelcomeBloc : ComposeScreen {
    fun onGetStartedClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        WelcomeScreen(bloc = this, modifier = modifier)
    }

    fun onSignInClicked()

    sealed class Output {
        /** Advance to the next onboarding step. */
        data object GetStarted : Output()

        /** The user already has an account and wants to sign in. */
        data object SignIn : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): WelcomeBloc
    }
}
