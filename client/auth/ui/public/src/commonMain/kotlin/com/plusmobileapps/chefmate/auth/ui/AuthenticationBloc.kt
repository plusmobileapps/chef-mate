package com.plusmobileapps.chefmate.auth.ui

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AuthenticationBloc : BackClickBloc, ComposeScreen {
    val models: StateFlow<Model>
    val email: StateFlow<String>
    val password: StateFlow<String>
    val confirmPassword: StateFlow<String>

    fun onEmailChanged(email: String)

    fun onPasswordChanged(password: String)

    fun onConfirmPasswordChanged(confirmPassword: String)

    fun onSubmitClicked()

    fun onToggleMode()

    fun onForgotPasswordClicked()

    fun onEmailMeACodeClicked()

    fun onUrlClicked(url: String)

    fun onDismissError()

    /** User confirmed the "you'll lose your guest recipes" dialog; proceed with sign-in. */
    fun onDiscardGuestDataConfirmed()

    /** User cancelled the "you'll lose your guest recipes" dialog; do nothing. */
    fun onDiscardGuestDataCancelled()

    data class Model(
        val mode: Mode = Mode.SignIn,
        val isLoading: Boolean = false,
        val errorMessage: TextData? = null,
        val emailError: TextData? = null,
        val passwordError: TextData? = null,
        val confirmPasswordError: TextData? = null,
        /**
         * When non-null, the user submitted Sign-In while currently anonymous and with guest
         * recipes on this device. UI shows a confirmation dialog; sign-in is gated on
         * [onDiscardGuestDataConfirmed].
         */
        val pendingGuestDataDiscard: PendingGuestDataDiscard? = null,
    ) {
        enum class Mode {
            SignIn,
            SignUp,
        }

        data class PendingGuestDataDiscard(val guestRecipeCount: Int)
    }

    sealed class Output {
        data object Finished : Output()

        data object AuthenticationSuccess : Output()

        data class EmailVerificationRequired(val email: String) : Output()

        /**
         * Fired when a currently-anonymous user signs up to upgrade their account. Routes to the
         * OTP screen with [com.plusmobileapps.chefmate.auth.data.OtpFlow.EmailChange] because
         * Supabase issues an email-change confirmation token (not a signup token) for the
         * underlying `updateUser{}` call.
         */
        data class EmailChangeRequired(val email: String) : Output()

        data class PasswordlessOtpSent(val email: String) : Output()

        data class OpenUrl(val url: String) : Output()
    }

    @Serializable
    enum class Props {
        SignIn,
        SignUp,
    }

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): AuthenticationBloc
    }
}
