package com.plusmobileapps.chefmate.auth.ui.impl.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.AuthenticationScreen
import com.plusmobileapps.chefmate.text.FixedString
import com.plusmobileapps.chefmate.text.TextData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import kotlinx.coroutines.flow.MutableStateFlow

private fun authBloc(
    model: AuthenticationBloc.Model,
    email: String = "",
    password: String = "",
    confirmPassword: String = "",
): AuthenticationBloc =
    object : AuthenticationBloc {
        override val models = MutableStateFlow(model)
        override val email = MutableStateFlow(email)
        override val password = MutableStateFlow(password)
        override val confirmPassword = MutableStateFlow(confirmPassword)

        override fun onEmailChanged(email: String) = Unit

        override fun onPasswordChanged(password: String) = Unit

        override fun onConfirmPasswordChanged(confirmPassword: String) = Unit

        override fun onSubmitClicked() = Unit

        override fun onToggleMode() = Unit

        override fun onForgotPasswordClicked() = Unit

        override fun onEmailMeACodeClicked() = Unit

        override fun onUrlClicked(url: String) = Unit

        override fun onDismissError() = Unit

        override fun onDiscardGuestDataConfirmed() = Unit

        override fun onDiscardGuestDataCancelled() = Unit

        override fun onBackClicked() = Unit
    }

private val previewPasswordRequirementsError: TextData =
    FixedString(
        "Password must include at least 6 characters, an uppercase letter, a number and a symbol."
    )

val previewAuthBlocSignUp: AuthenticationBloc =
    authBloc(model = AuthenticationBloc.Model(mode = AuthenticationBloc.Model.Mode.SignUp))

val previewAuthBlocSignUpWithPasswordError: AuthenticationBloc =
    authBloc(
        model =
            AuthenticationBloc.Model(
                mode = AuthenticationBloc.Model.Mode.SignUp,
                passwordError = previewPasswordRequirementsError,
            ),
        email = "user@example.com",
        password = "abc",
        confirmPassword = "abc",
    )

val previewAuthBlocSignUpWithDialogError: AuthenticationBloc =
    authBloc(
        model =
            AuthenticationBloc.Model(
                mode = AuthenticationBloc.Model.Mode.SignUp,
                errorMessage =
                    FixedString(
                        "That password isn't strong enough. Use a mix of uppercase and lowercase " +
                            "letters, numbers, and symbols, with at least 6 characters."
                    ),
            ),
        email = "user@example.com",
        password = "Abc1234!",
        confirmPassword = "Abc1234!",
    )

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AuthenticationScreenSignUpPreview() {
    ChefMateTheme { AuthenticationScreen(bloc = previewAuthBlocSignUp) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AuthenticationScreenSignUpWithPasswordErrorPreview() {
    ChefMateTheme { AuthenticationScreen(bloc = previewAuthBlocSignUpWithPasswordError) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AuthenticationScreenSignUpWithDialogErrorPreview() {
    ChefMateTheme { AuthenticationScreen(bloc = previewAuthBlocSignUpWithDialogError) }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
internal fun AuthenticationScreenSignUpWithPasswordErrorDarkPreview() {
    ChefMateTheme(darkTheme = true) {
        AuthenticationScreen(bloc = previewAuthBlocSignUpWithPasswordError)
    }
}
