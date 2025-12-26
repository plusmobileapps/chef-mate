package com.plusmobileapps.chefmate.auth.ui

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AuthenticationBloc : BackClickBloc {
    val models: StateFlow<Model>
    val email: StateFlow<String>
    val password: StateFlow<String>
    val confirmPassword: StateFlow<String>

    fun onEmailChanged(email: String)

    fun onPasswordChanged(password: String)

    fun onConfirmPasswordChanged(confirmPassword: String)

    fun onSignInClicked()

    fun onSignUpClicked()

    fun onToggleMode()

    fun onForgotPasswordClicked()

    data class Model(
        val mode: Mode = Mode.SignIn,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) {
        enum class Mode {
            SignIn,
            SignUp,
        }
    }

    sealed class Output {
        data object Finished : Output()

        data object AuthenticationSuccess : Output()
    }

    @Serializable
    enum class Props {
        SignIn,
        SignUp,
    }

    fun interface Factory {
        fun create(
            context: BlocContext,
            props: Props,
            output: Consumer<Output>,
        ): AuthenticationBloc
    }
}
