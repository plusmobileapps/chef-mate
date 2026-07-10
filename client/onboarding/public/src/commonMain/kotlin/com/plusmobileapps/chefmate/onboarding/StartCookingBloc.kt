package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.ui.ComposeScreen

/** Final screen of the onboarding flow — confirms setup and launches the rest of the app. */
interface StartCookingBloc : ComposeScreen {
    /**
     * Whether to offer a sign-up button. False when the flow is re-entered by an already signed-in
     * user, who can't sign up again. Defaults to true so previews and simple fakes need no changes.
     */
    val showSignUp: Boolean
        get() = true

    fun onStartCookingClicked()

    fun onSignUpClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        StartCookingScreen(bloc = this, modifier = modifier)
    }

    sealed class Output {
        /** The user is done with onboarding and wants to start using the app. */
        data object StartCooking : Output()

        /** The user wants to create an account; the root should open the sign-up flow. */
        data object SignUp : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            showSignUp: Boolean,
            output: Consumer<Output>,
        ): StartCookingBloc
    }
}
