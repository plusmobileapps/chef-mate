package com.plusmobileapps.chefmate.settings

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer

interface SettingsBloc {
    fun onSignInClicked()

    fun onSignUpClicked()

    fun onSignOutClicked()

    sealed class Output {
        data object OpenSignUp : Output()

        data object OpenSignIn : Output()
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            output: Consumer<Output>,
        ): SettingsBloc
    }
}
