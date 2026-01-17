package com.plusmobileapps.chefmate.auth.ui.impl

import chefmate.client.auth.ui.impl.generated.resources.Res
import chefmate.client.auth.ui.impl.generated.resources.auth_error_authentication_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_confirm_password_required
import chefmate.client.auth.ui.impl.generated.resources.auth_error_email_required
import chefmate.client.auth.ui.impl.generated.resources.auth_error_invalid_credentials
import chefmate.client.auth.ui.impl.generated.resources.auth_error_invalid_email
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_required
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_reset_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_passwords_do_not_match
import chefmate.client.auth.ui.impl.generated.resources.auth_error_sign_up_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_user_already_exists
import chefmate.client.auth.ui.impl.generated.resources.auth_success_password_reset_sent
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.util.EmailUtil
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import kotlin.coroutines.CoroutineContext

@Inject
class AuthenticationViewModel(
    @Assisted initialProps: AuthenticationBloc.Props,
    @Main mainContext: CoroutineContext,
    private val authRepository: AuthenticationRepository,
    private val emailUtil: EmailUtil,
) : ViewModel(mainContext) {
    private val _state =
        MutableStateFlow(
            State(
                mode =
                    when (initialProps) {
                        AuthenticationBloc.Props.SignIn -> AuthenticationBloc.Model.Mode.SignIn
                        AuthenticationBloc.Props.SignUp -> AuthenticationBloc.Model.Mode.SignUp
                    },
            ),
        )
    private val _email = MutableStateFlow("")
    private val _password = MutableStateFlow("")
    private val _confirmPassword = MutableStateFlow("")
    private val output = Channel<Output>(Channel.BUFFERED)

    val state: StateFlow<State> = _state.asStateFlow()
    val email: StateFlow<String> = _email.asStateFlow()
    val password: StateFlow<String> = _password.asStateFlow()
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    val outputs: Flow<Output> = output.receiveAsFlow()

    override fun onCleared() {
        super.onCleared()
        output.close()
    }

    fun onEmailChanged(email: String) {
        _email.value = email
        // Clear errors when user starts typing
        if (_state.value.errorMessage != null || _state.value.emailError != null) {
            _state.value = _state.value.copy(errorMessage = null, emailError = null)
        }
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
        // Clear error when user starts typing
        if (_state.value.errorMessage != null) {
            _state.value = _state.value.copy(errorMessage = null)
        }
    }

    fun onConfirmPasswordChanged(confirmPassword: String) {
        _confirmPassword.value = confirmPassword
        // Clear errors when user starts typing
        if (_state.value.errorMessage != null || _state.value.confirmPasswordError != null) {
            _state.value = _state.value.copy(errorMessage = null, confirmPasswordError = null)
        }
    }

    fun onToggleMode() {
        val newMode =
            when (_state.value.mode) {
                AuthenticationBloc.Model.Mode.SignIn -> AuthenticationBloc.Model.Mode.SignUp
                AuthenticationBloc.Model.Mode.SignUp -> AuthenticationBloc.Model.Mode.SignIn
            }
        _state.value =
            _state.value.copy(
                mode = newMode,
                errorMessage = null,
                emailError = null,
                confirmPasswordError = null,
            )
    }

    fun onSubmitClicked() {
        when (_state.value.mode) {
            AuthenticationBloc.Model.Mode.SignIn -> signIn()
            AuthenticationBloc.Model.Mode.SignUp -> signUp()
        }
    }

    private fun signIn() {
        val email = _email.value
        val password = _password.value

        // Validate input
        if (email.isBlank()) {
            _state.value =
                _state.value.copy(
                    emailError = ResourceString(Res.string.auth_error_email_required),
                )
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(
                    emailError = ResourceString(Res.string.auth_error_invalid_email),
                )
            return
        }
        if (password.isBlank()) {
            _state.value =
                _state.value.copy(
                    errorMessage = ResourceString(Res.string.auth_error_password_required),
                )
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, emailError = null)

        scope.launch {
            val result = authRepository.signInWithEmailAndPassword(email, password)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false)
                    output.send(Output.AuthenticationSuccess)
                },
                onFailure = { e ->
                    Napier.e("Authentication failed", e)
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage = getSignInErrorMessage(e),
                        )
                },
            )
        }
    }

    private fun signUp() {
        val email = _email.value
        val password = _password.value
        val confirmPassword = _confirmPassword.value

        // Validate input
        if (email.isBlank()) {
            _state.value =
                _state.value.copy(
                    emailError = ResourceString(Res.string.auth_error_email_required),
                )
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(
                    emailError = ResourceString(Res.string.auth_error_invalid_email),
                )
            return
        }
        if (password.isBlank()) {
            _state.value =
                _state.value.copy(
                    errorMessage = ResourceString(Res.string.auth_error_password_required),
                )
            return
        }
        if (confirmPassword.isBlank()) {
            _state.value =
                _state.value.copy(
                    confirmPasswordError = ResourceString(Res.string.auth_error_confirm_password_required),
                )
            return
        }
        if (password != confirmPassword) {
            _state.value =
                _state.value.copy(
                    confirmPasswordError = ResourceString(Res.string.auth_error_passwords_do_not_match),
                )
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, emailError = null, confirmPasswordError = null)

        scope.launch {
            val result = authRepository.signUpWithEmailAndPassword(email, password)
            result.fold(
                onSuccess = { signUpResult ->
                    when (signUpResult) {
                        SignUpResult.Success -> {
                            _state.value = _state.value.copy(isLoading = false)
                            output.send(Output.AuthenticationSuccess)
                        }
                        SignUpResult.AwaitingEmailVerification -> {
                            _state.value = _state.value.copy(isLoading = false)
                            output.send(Output.EmailVerificationRequired(email))
                        }
                        SignUpResult.UserAlreadyExists -> {
                            _state.value =
                                _state.value.copy(
                                    isLoading = false,
                                    errorMessage = ResourceString(Res.string.auth_error_user_already_exists),
                                )
                        }
                    }
                },
                onFailure = { e ->
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage =
                                e.message?.let { FixedString(it) }
                                    ?: ResourceString(Res.string.auth_error_sign_up_failed),
                        )
                },
            )
        }
    }

    fun forgotPassword() {
        val email = _email.value

        // Validate input
        if (email.isBlank()) {
            _state.value =
                _state.value.copy(
                    emailError = ResourceString(Res.string.auth_error_email_required),
                )
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(
                    emailError = ResourceString(Res.string.auth_error_invalid_email),
                )
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, emailError = null)

        scope.launch {
            val result = authRepository.sendPasswordResetEmail(email)
            result.fold(
                onSuccess = {
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage = ResourceString(Res.string.auth_success_password_reset_sent),
                        )
                },
                onFailure = { e ->
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage =
                                e.message?.let { FixedString(it) }
                                    ?: ResourceString(Res.string.auth_error_password_reset_failed),
                        )
                },
            )
        }
    }

    fun onDismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    /**
     * Determines the appropriate error message for sign-in failures.
     * Supabase returns "Invalid login credentials" for wrong email/password combinations.
     */
    private fun getSignInErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("invalid") && message.contains("credentials") ->
                ResourceString(Res.string.auth_error_invalid_credentials)
            message.contains("invalid login") ->
                ResourceString(Res.string.auth_error_invalid_credentials)
            else ->
                ResourceString(Res.string.auth_error_authentication_failed)
        }
    }

    data class State(
        val mode: AuthenticationBloc.Model.Mode = AuthenticationBloc.Model.Mode.SignIn,
        val isLoading: Boolean = false,
        val errorMessage: TextData? = null,
        val emailError: TextData? = null,
        val confirmPasswordError: TextData? = null,
    )

    sealed class Output {
        data object AuthenticationSuccess : Output()

        data class EmailVerificationRequired(
            val email: String,
        ) : Output()
    }
}
