package com.plusmobileapps.chefmate.auth.ui.impl.otp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun otpBloc(model: OtpBloc.Model, code: String = ""): OtpBloc =
    object : OtpBloc {
        override val models = MutableStateFlow(model)
        override val code = MutableStateFlow(code)

        override fun onCodeChanged(code: String) = Unit

        override fun onVerifyClicked() = Unit

        override fun onResendClicked() = Unit

        override fun onDismissError() = Unit

        override fun onBackClicked() = Unit

        @Composable override fun Content(modifier: Modifier) = OtpScreen(this, modifier)
    }

val previewOtpBlocSignUp: OtpBloc =
    otpBloc(OtpBloc.Model(email = "test@example.com", flow = OtpFlow.SignUp))

val previewOtpBlocPasswordless: OtpBloc =
    otpBloc(
        OtpBloc.Model(email = "test@example.com", flow = OtpFlow.PasswordlessSignIn),
        code = "1234",
    )

val previewOtpBlocLoading: OtpBloc =
    otpBloc(
        OtpBloc.Model(email = "test@example.com", flow = OtpFlow.SignUp, isLoading = true),
        code = "123456",
    )

val previewOtpBlocError: OtpBloc =
    otpBloc(
        OtpBloc.Model(
            email = "test@example.com",
            flow = OtpFlow.SignUp,
            errorMessage = FixedString("That code doesn’t match. Double-check and try again."),
        ),
        code = "123456",
    )

val previewOtpBlocCountdown: OtpBloc =
    otpBloc(
        OtpBloc.Model(
            email = "test@example.com",
            flow = OtpFlow.PasswordlessSignIn,
            resendCountdownSeconds = 23,
            infoMessage = FixedString("A fresh code is on its way."),
        ),
        code = "12",
    )

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun OtpScreenSignUpPreview() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocSignUp) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun OtpScreenPasswordlessPreview() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocPasswordless) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun OtpScreenLoadingPreview() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocLoading) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun OtpScreenErrorPreview() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocError) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun OtpScreenCountdownPreview() {
    ChefMateTheme { OtpScreen(bloc = previewOtpBlocCountdown) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun OtpScreenSignUpDarkPreview() {
    ChefMateTheme(darkTheme = true) { OtpScreen(bloc = previewOtpBlocSignUp) }
}
