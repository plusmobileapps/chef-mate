package com.plusmobileapps.chefmate.auth.ui.impl

import chefmate.client.auth.ui.impl.generated.resources.Res
import chefmate.client.auth.ui.impl.generated.resources.auth_error_authentication_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_confirm_password_required
import chefmate.client.auth.ui.impl.generated.resources.auth_error_email_required
import chefmate.client.auth.ui.impl.generated.resources.auth_error_invalid_credentials
import chefmate.client.auth.ui.impl.generated.resources.auth_error_invalid_email
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_missing
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_required
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_reset_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_reset_rate_limit
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_reset_user_not_found
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_too_weak
import chefmate.client.auth.ui.impl.generated.resources.auth_error_passwords_do_not_match
import chefmate.client.auth.ui.impl.generated.resources.auth_error_send_otp_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_send_otp_rate_limit
import chefmate.client.auth.ui.impl.generated.resources.auth_error_sign_up_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_error_user_already_exists
import chefmate.client.auth.ui.impl.generated.resources.auth_password_req_digit
import chefmate.client.auth.ui.impl.generated.resources.auth_password_req_lowercase
import chefmate.client.auth.ui.impl.generated.resources.auth_password_req_min_length
import chefmate.client.auth.ui.impl.generated.resources.auth_password_req_symbol
import chefmate.client.auth.ui.impl.generated.resources.auth_password_req_uppercase
import chefmate.client.auth.ui.impl.generated.resources.auth_success_password_reset_sent
import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc.Model.Mode.SignIn
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc.Model.Mode.SignUp
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc.Model.PendingGuestDataDiscard
import com.plusmobileapps.chefmate.auth.usecase.SignInUseCase
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.text.JoinedTextData
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.util.EmailUtil
import com.plusmobileapps.chefmate.util.PasswordValidator
import com.plusmobileapps.chefmate.util.PasswordValidator.Requirement
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@AssistedInject
class AuthenticationViewModel(
    @Assisted initialProps: AuthenticationBloc.Props,
    @Main mainContext: CoroutineContext,
    private val authRepository: AuthenticationRepository,
    private val signInUseCase: SignInUseCase,
    private val emailUtil: EmailUtil,
    private val passwordValidator: PasswordValidator,
) : ViewModel(mainContext) {
    private val _state =
        MutableStateFlow(
            State(
                mode =
                    when (initialProps) {
                        AuthenticationBloc.Props.SignIn -> SignIn
                        AuthenticationBloc.Props.SignUp -> SignUp
                    }
            )
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
        // Clear errors when user starts typing
        if (_state.value.errorMessage != null || _state.value.passwordError != null) {
            _state.value = _state.value.copy(errorMessage = null, passwordError = null)
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
                SignIn -> SignUp
                SignUp -> SignIn
            }
        _state.value =
            _state.value.copy(
                mode = newMode,
                errorMessage = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
            )
    }

    fun onSubmitClicked() {
        when (_state.value.mode) {
            SignIn -> signIn()
            SignUp -> signUp()
        }
    }

    private fun signIn() {
        val email = _email.value
        val password = _password.value

        // Validate input
        if (email.isBlank()) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_email_required.asTextData())
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_invalid_email.asTextData())
            return
        }
        if (password.isBlank()) {
            _state.value =
                _state.value.copy(
                    errorMessage = Res.string.auth_error_password_required.asTextData()
                )
            return
        }

        // If the user is currently anonymous with guest data on this device, sign-in will
        // discard it (their anon recipes get orphaned on the server, cleaned up by the
        // periodic sweep). Gate behind an explicit confirmation so they don't lose recipes
        // they didn't realize were "guest".
        scope.launch {
            val guestRecipeCount = signInUseCase.guestRecipesToDiscard()
            if (guestRecipeCount > 0) {
                _state.value =
                    _state.value.copy(
                        errorMessage = null,
                        emailError = null,
                        pendingGuestDataDiscard = PendingGuestDataDiscard(guestRecipeCount),
                    )
                return@launch
            }
            performSignIn(email, password)
        }
    }

    fun onDiscardGuestDataConfirmed() {
        if (_state.value.pendingGuestDataDiscard == null) return
        scope.launch { performSignIn(_email.value, _password.value) }
    }

    fun onDiscardGuestDataCancelled() {
        _state.value = _state.value.copy(pendingGuestDataDiscard = null)
    }

    private suspend fun performSignIn(email: String, password: String) {
        _state.value =
            _state.value.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null,
                pendingGuestDataDiscard = null,
            )
        val result = signInUseCase(email, password)
        result.fold(
            onSuccess = {
                _state.value = _state.value.copy(isLoading = false)
                output.send(Output.AuthenticationSuccess)
            },
            onFailure = { e ->
                Logger.e("Authentication failed", e)
                _state.value =
                    _state.value.copy(isLoading = false, errorMessage = getSignInErrorMessage(e))
            },
        )
    }

    private fun signUp() {
        val email = _email.value
        val password = _password.value
        val confirmPassword = _confirmPassword.value

        // Validate input
        if (email.isBlank()) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_email_required.asTextData())
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_invalid_email.asTextData())
            return
        }
        if (password.isBlank()) {
            _state.value =
                _state.value.copy(
                    errorMessage = Res.string.auth_error_password_required.asTextData()
                )
            return
        }
        val passwordResult = passwordValidator.validate(password)
        if (passwordResult is PasswordValidator.Result.Invalid) {
            _state.value =
                _state.value.copy(
                    passwordError = buildPasswordRequirementsMessage(passwordResult.missing)
                )
            return
        }
        if (confirmPassword.isBlank()) {
            _state.value =
                _state.value.copy(
                    confirmPasswordError =
                        Res.string.auth_error_confirm_password_required.asTextData()
                )
            return
        }
        if (password != confirmPassword) {
            _state.value =
                _state.value.copy(
                    confirmPasswordError = Res.string.auth_error_passwords_do_not_match.asTextData()
                )
            return
        }

        _state.value =
            _state.value.copy(
                isLoading = true,
                errorMessage = null,
                emailError = null,
                passwordError = null,
                confirmPasswordError = null,
            )

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
                        SignUpResult.AwaitingEmailChange -> {
                            _state.value = _state.value.copy(isLoading = false)
                            output.send(Output.EmailChangeRequired(email))
                        }
                        SignUpResult.UserAlreadyExists -> {
                            _state.value =
                                _state.value.copy(
                                    isLoading = false,
                                    errorMessage =
                                        Res.string.auth_error_user_already_exists.asTextData(),
                                )
                        }
                    }
                },
                onFailure = { e ->
                    Logger.e("Sign up failed", e)
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage = getSignUpErrorMessage(e),
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
                _state.value.copy(emailError = Res.string.auth_error_email_required.asTextData())
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_invalid_email.asTextData())
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
                            errorMessage = Res.string.auth_success_password_reset_sent.asTextData(),
                        )
                },
                onFailure = { e ->
                    Logger.e("Password reset failed", e)
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage = getPasswordResetErrorMessage(e),
                        )
                },
            )
        }
    }

    fun onEmailMeACodeClicked() {
        val email = _email.value

        if (email.isBlank()) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_email_required.asTextData())
            return
        }
        if (!emailUtil.isValidEmail(email)) {
            _state.value =
                _state.value.copy(emailError = Res.string.auth_error_invalid_email.asTextData())
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, emailError = null)

        scope.launch {
            val result = authRepository.sendSignInOtp(email)
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false)
                    output.send(Output.PasswordlessOtpSent(email))
                },
                onFailure = { e ->
                    Logger.e("Send sign-in OTP failed", e)
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage = getOtpSendErrorMessage(e),
                        )
                },
            )
        }
    }

    fun onDismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun onUrlClicked(url: String) {
        scope.launch { output.send(Output.OpenUrl(url)) }
    }

    /**
     * Determines the appropriate error message for sign-in failures. Supabase returns "Invalid
     * login credentials" for wrong email/password combinations.
     */
    private fun getSignInErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("invalid") && message.contains("credentials") ->
                Res.string.auth_error_invalid_credentials.asTextData()
            message.contains("invalid login") ->
                Res.string.auth_error_invalid_credentials.asTextData()
            else -> Res.string.auth_error_authentication_failed.asTextData()
        }
    }

    /**
     * Determines the appropriate error message for password reset failures. Supabase may return
     * rate limiting errors or user not found errors.
     */
    private fun getPasswordResetErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("rate") || message.contains("too many") || message.contains("limit") ->
                Res.string.auth_error_password_reset_rate_limit.asTextData()
            message.contains("not found") || message.contains("no user") ->
                Res.string.auth_error_password_reset_user_not_found.asTextData()
            else -> Res.string.auth_error_password_reset_failed.asTextData()
        }
    }

    /**
     * Maps Supabase sign-up failures to a friendly dialog message. The Supabase password-complexity
     * error looks like "Password should contain at least one character of each: ..." — we surface
     * [auth_error_password_too_weak] for that case. Other failures fall back to the generic sign-up
     * failed message rather than leaking the raw exception text to the user.
     */
    private fun getSignUpErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        val isPasswordComplexity =
            message.contains("password") &&
                (message.contains("contain") || message.contains("character"))
        return if (isPasswordComplexity) {
            Res.string.auth_error_password_too_weak.asTextData()
        } else {
            Res.string.auth_error_sign_up_failed.asTextData()
        }
    }

    private fun buildPasswordRequirementsMessage(missing: Set<Requirement>): TextData {
        val parts =
            Requirement.entries.filter { it in missing }.map { ResourceString(it.resource()) }
        return PhraseModel(
            Res.string.auth_error_password_missing,
            "requirements" to JoinedTextData(parts),
        )
    }

    private fun Requirement.resource(): StringResource =
        when (this) {
            Requirement.MinLength -> Res.string.auth_password_req_min_length
            Requirement.Lowercase -> Res.string.auth_password_req_lowercase
            Requirement.Uppercase -> Res.string.auth_password_req_uppercase
            Requirement.Digit -> Res.string.auth_password_req_digit
            Requirement.Symbol -> Res.string.auth_password_req_symbol
        }

    private fun getOtpSendErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("rate") || message.contains("too many") || message.contains("limit") ->
                Res.string.auth_error_send_otp_rate_limit.asTextData()
            else -> Res.string.auth_error_send_otp_failed.asTextData()
        }
    }

    data class State(
        val mode: AuthenticationBloc.Model.Mode = SignIn,
        val isLoading: Boolean = false,
        val errorMessage: TextData? = null,
        val emailError: TextData? = null,
        val passwordError: TextData? = null,
        val confirmPasswordError: TextData? = null,
        val pendingGuestDataDiscard: PendingGuestDataDiscard? = null,
    )

    sealed class Output {
        data object AuthenticationSuccess : Output()

        data class EmailVerificationRequired(val email: String) : Output()

        data class EmailChangeRequired(val email: String) : Output()

        data class PasswordlessOtpSent(val email: String) : Output()

        data class OpenUrl(val url: String) : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(initialProps: AuthenticationBloc.Props): AuthenticationViewModel
    }
}
