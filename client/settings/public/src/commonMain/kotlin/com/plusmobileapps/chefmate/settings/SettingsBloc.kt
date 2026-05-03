package com.plusmobileapps.chefmate.settings

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import kotlinx.coroutines.flow.StateFlow

interface SettingsBloc {
    val state: StateFlow<Model>

    fun onSignInClicked()

    fun onSignUpClicked()

    fun onSignOutClicked()

    fun onSignOutConfirmed()

    fun onSignOutDismissed()

    fun onUrlClicked(url: String)

    fun onAppSettingsClicked()

    data class Model(
        val isAuthenticated: Boolean = false,
        val greeting: TextData? = null,
        val verificationMessage: TextData? = null,
        val showSignOutConfirmationDialog: Boolean = false,
    )

    sealed class Output {
        data object OpenSignUp : Output()

        data object OpenSignIn : Output()

        data object OpenAppSettings : Output()

        data class OpenUrl(val url: String) : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): SettingsBloc
    }
}
