package com.plusmobileapps.chefmate.auth.ui.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.di.Main
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
    // TODO: Add AuthRepository parameter when available
    // private val repository: AuthRepository,
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
        // Clear error when user starts typing
        if (_state.value.errorMessage != null) {
            _state.value = _state.value.copy(errorMessage = null)
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
        // Clear error when user starts typing
        if (_state.value.errorMessage != null) {
            _state.value = _state.value.copy(errorMessage = null)
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
            )
    }

    fun signIn() {
        val email = _email.value
        val password = _password.value

        // Validate input
        if (email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Email is required")
            return
        }
        if (password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Password is required")
            return
        }

        // TODO: Implement actual authentication logic
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        scope.launch {
            try {
                // TODO: Call repository.signIn(email, password)
                // For now, simulate success
                _state.value = _state.value.copy(isLoading = false)
                output.send(Output.AuthenticationSuccess)
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Authentication failed",
                    )
            }
        }
    }

    fun signUp() {
        val email = _email.value
        val password = _password.value
        val confirmPassword = _confirmPassword.value

        // Validate input
        if (email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Email is required")
            return
        }
        if (password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Password is required")
            return
        }
        if (confirmPassword.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Please confirm your password")
            return
        }
        if (password != confirmPassword) {
            _state.value = _state.value.copy(errorMessage = "Passwords do not match")
            return
        }

        // TODO: Implement actual sign up logic
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        scope.launch {
            try {
                // TODO: Call repository.signUp(email, password)
                // For now, simulate success
                _state.value = _state.value.copy(isLoading = false)
                output.send(Output.AuthenticationSuccess)
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Sign up failed",
                    )
            }
        }
    }

    fun forgotPassword() {
        val email = _email.value

        // Validate input
        if (email.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Email is required")
            return
        }

        // TODO: Implement forgot password logic
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        scope.launch {
            try {
                // TODO: Call repository.sendPasswordResetEmail(email)
                // For now, simulate success
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = "Password reset email sent",
                    )
            } catch (e: Exception) {
                _state.value =
                    _state.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to send password reset email",
                    )
            }
        }
    }

    data class State(
        val mode: AuthenticationBloc.Model.Mode = AuthenticationBloc.Model.Mode.SignIn,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    )

    sealed class Output {
        data object AuthenticationSuccess : Output()
    }
}
