package com.plusmobileapps.chefmate.auth.ui.impl.otp

import chefmate.client.auth.ui.impl.generated.resources.Res
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_expired
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_invalid_length
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_invalid_token
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_rate_limit
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_required
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_resend_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_error_verification_failed
import chefmate.client.auth.ui.impl.generated.resources.auth_otp_resend_success
import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.text.asTextData
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AssistedInject
class OtpViewModel(
    @Assisted props: OtpBloc.Props,
    @Main mainContext: CoroutineContext,
    private val authRepository: AuthenticationRepository,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State(email = props.email, flow = props.flow))
    private val _code = MutableStateFlow("")
    private val output = Channel<Output>(Channel.BUFFERED)
    private var resendCountdownJob: Job? = null

    val state: StateFlow<State> = _state.asStateFlow()
    val code: StateFlow<String> = _code.asStateFlow()
    val outputs: Flow<Output> = output.receiveAsFlow()

    override fun onCleared() {
        super.onCleared()
        output.close()
    }

    fun onCodeChanged(code: String) {
        val sanitized = code.filter(Char::isDigit).take(OTP_CODE_LENGTH)
        _code.value = sanitized
        if (_state.value.errorMessage != null) {
            _state.value = _state.value.copy(errorMessage = null)
        }
    }

    fun onVerifyClicked() {
        val token = _code.value
        if (token.isBlank()) {
            _state.value =
                _state.value.copy(errorMessage = Res.string.auth_otp_error_required.asTextData())
            return
        }
        if (token.length != OTP_CODE_LENGTH) {
            _state.value =
                _state.value.copy(
                    errorMessage = Res.string.auth_otp_error_invalid_length.asTextData()
                )
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null, infoMessage = null)

        scope.launch {
            val result =
                authRepository.verifyEmailOtp(
                    email = _state.value.email,
                    token = token,
                    flow = _state.value.flow,
                )
            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false)
                    output.send(Output.Verified)
                },
                onFailure = { e ->
                    Logger.e("OTP verification failed", e)
                    _state.value =
                        _state.value.copy(
                            isLoading = false,
                            errorMessage = getOtpVerifyErrorMessage(e),
                        )
                },
            )
        }
    }

    fun onResendClicked() {
        if (_state.value.resendCountdownSeconds > 0 || _state.value.isLoading) return
        _state.value = _state.value.copy(errorMessage = null, infoMessage = null)

        scope.launch {
            val result =
                authRepository.resendOtp(email = _state.value.email, flow = _state.value.flow)
            result.fold(
                onSuccess = {
                    _state.value =
                        _state.value.copy(
                            infoMessage = Res.string.auth_otp_resend_success.asTextData()
                        )
                    startResendCountdown()
                },
                onFailure = { e ->
                    Logger.e("OTP resend failed", e)
                    _state.value = _state.value.copy(errorMessage = getOtpResendErrorMessage(e))
                },
            )
        }
    }

    fun onDismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun startResendCountdown(seconds: Int = RESEND_COUNTDOWN_SECONDS) {
        resendCountdownJob?.cancel()
        _state.value = _state.value.copy(resendCountdownSeconds = seconds)
        resendCountdownJob = scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1_000)
                remaining -= 1
                _state.value = _state.value.copy(resendCountdownSeconds = remaining)
            }
        }
    }

    private fun getOtpVerifyErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("expired") -> Res.string.auth_otp_error_expired.asTextData()
            message.contains("rate") || message.contains("too many") || message.contains("limit") ->
                Res.string.auth_otp_error_rate_limit.asTextData()
            message.contains("invalid") || message.contains("incorrect") ->
                Res.string.auth_otp_error_invalid_token.asTextData()
            else -> Res.string.auth_otp_error_verification_failed.asTextData()
        }
    }

    private fun getOtpResendErrorMessage(e: Throwable): TextData {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("rate") || message.contains("too many") || message.contains("limit") ->
                Res.string.auth_otp_error_rate_limit.asTextData()
            else -> Res.string.auth_otp_error_resend_failed.asTextData()
        }
    }

    data class State(
        val email: String,
        val flow: OtpFlow,
        val isLoading: Boolean = false,
        val errorMessage: TextData? = null,
        val infoMessage: TextData? = null,
        val resendCountdownSeconds: Int = 0,
    )

    sealed class Output {
        data object Verified : Output()
    }

    @AssistedFactory
    fun interface Factory {
        fun create(props: OtpBloc.Props): OtpViewModel
    }

    companion object {
        const val RESEND_COUNTDOWN_SECONDS = 30
        const val OTP_CODE_LENGTH = 8
    }
}
