package com.plusmobileapps.chefmate.auth.ui.otp

import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.ComposeScreen
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface OtpBloc : BackClickBloc, ComposeScreen {
    val models: StateFlow<Model>
    val code: StateFlow<String>

    fun onCodeChanged(code: String)

    fun onVerifyClicked()

    fun onResendClicked()

    fun onDismissError()

    data class Model(
        val email: String,
        val flow: OtpFlow,
        val isLoading: Boolean = false,
        val errorMessage: TextData? = null,
        val infoMessage: TextData? = null,
        val resendCountdownSeconds: Int = 0,
    )

    sealed class Output {
        data object Verified : Output()

        data object Cancelled : Output()
    }

    @Serializable data class Props(val email: String, val flow: OtpFlow)

    fun interface Factory {
        fun create(context: BlocContext, props: Props, output: Consumer<Output>): OtpBloc
    }
}
