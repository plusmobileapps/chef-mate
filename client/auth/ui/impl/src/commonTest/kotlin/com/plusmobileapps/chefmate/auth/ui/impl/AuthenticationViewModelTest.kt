@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.auth.ui.impl

import chefmate.client.auth.ui.impl.generated.resources.Res
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_missing
import chefmate.client.auth.ui.impl.generated.resources.auth_error_password_too_weak
import chefmate.client.auth.ui.impl.generated.resources.auth_error_sign_up_failed
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.text.PhraseModel
import com.plusmobileapps.chefmate.text.ResourceString
import com.plusmobileapps.chefmate.util.EmailUtil
import com.plusmobileapps.chefmate.util.PasswordValidator
import com.plusmobileapps.chefmate.util.PasswordValidator.Requirement
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class AuthenticationViewModelTest {

    private class FakeEmailUtil(private val valid: Boolean = true) : EmailUtil {
        override fun isValidEmail(email: String): Boolean = valid
    }

    private class FakePasswordValidator(
        var result: PasswordValidator.Result = PasswordValidator.Result.Valid,
        var lastInput: String? = null,
    ) : PasswordValidator {
        override fun validate(password: String): PasswordValidator.Result {
            lastInput = password
            return result
        }
    }

    private fun TestScope.viewModel(
        repo: FakeAuthenticationRepository = FakeAuthenticationRepository(),
        passwordValidator: PasswordValidator = FakePasswordValidator(),
        emailUtil: EmailUtil = FakeEmailUtil(),
    ): AuthenticationViewModel =
        AuthenticationViewModel(
            initialProps = AuthenticationBloc.Props.SignUp,
            mainContext = UnconfinedTestDispatcher(testScheduler),
            authRepository = repo,
            emailUtil = emailUtil,
            passwordValidator = passwordValidator,
        )

    @Test
    fun When_sign_up_with_weak_password_Then_inline_password_error_set_and_repo_not_called() =
        runTest {
            val repo = FakeAuthenticationRepository()
            // The validator says the password is missing uppercase + symbol.
            val passwordValidator =
                FakePasswordValidator(
                    result =
                        PasswordValidator.Result.Invalid(
                            setOf(Requirement.Uppercase, Requirement.Symbol)
                        )
                )
            val viewModel = viewModel(repo = repo, passwordValidator = passwordValidator)
            // Use a non-trivial signUpResult so we can prove the repo was NOT called: if it were,
            // the
            // success path would clear isLoading and we'd see no passwordError.
            repo.signUpResult = Result.failure(IllegalStateException("should not be called"))

            viewModel.onEmailChanged("user@example.com")
            viewModel.onPasswordChanged("abc123")
            viewModel.onConfirmPasswordChanged("abc123")
            viewModel.onSubmitClicked()

            viewModel.state.value.passwordError shouldNotBe null
            // The message is built via PhraseModel referencing the password_missing template.
            val phrase = viewModel.state.value.passwordError.shouldBeInstanceOf<PhraseModel>()
            phrase.resource shouldBe Res.string.auth_error_password_missing
            viewModel.state.value.errorMessage shouldBe null
            viewModel.state.value.isLoading shouldBe false
            passwordValidator.lastInput shouldBe "abc123"
        }

    @Test
    fun When_user_types_in_password_field_Then_passwordError_clears() = runTest {
        val passwordValidator =
            FakePasswordValidator(
                result = PasswordValidator.Result.Invalid(setOf(Requirement.Symbol))
            )
        val viewModel = viewModel(passwordValidator = passwordValidator)

        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("Abcdef1")
        viewModel.onConfirmPasswordChanged("Abcdef1")
        viewModel.onSubmitClicked()

        viewModel.state.value.passwordError shouldNotBe null

        viewModel.onPasswordChanged("Abcdef1!")

        viewModel.state.value.passwordError shouldBe null
    }

    @Test
    fun When_supabase_returns_password_complexity_error_Then_dialog_shows_too_weak_message() =
        runTest {
            val repo = FakeAuthenticationRepository()
            repo.signUpResult =
                Result.failure(
                    IllegalStateException(
                        "Password should contain at least one character of each: " +
                            "abcdefghijklmnopqrstuvwxyz, ABCDEFGHIJKLMNOPQRSTUVWXYZ, 0123456789."
                    )
                )
            // Local validator passes — the failure must come from the server branch.
            val viewModel = viewModel(repo = repo, passwordValidator = FakePasswordValidator())

            viewModel.onEmailChanged("user@example.com")
            viewModel.onPasswordChanged("Abc1234!")
            viewModel.onConfirmPasswordChanged("Abc1234!")
            viewModel.onSubmitClicked()

            viewModel.state.value.errorMessage shouldBe
                ResourceString(Res.string.auth_error_password_too_weak)
            viewModel.state.value.isLoading shouldBe false
        }

    @Test
    fun When_supabase_returns_unrelated_error_Then_dialog_shows_generic_sign_up_failed() = runTest {
        val repo = FakeAuthenticationRepository()
        repo.signUpResult = Result.failure(IllegalStateException("Network unreachable"))
        val viewModel = viewModel(repo = repo)

        viewModel.onEmailChanged("user@example.com")
        viewModel.onPasswordChanged("Abc1234!")
        viewModel.onConfirmPasswordChanged("Abc1234!")
        viewModel.onSubmitClicked()

        viewModel.state.value.errorMessage shouldBe
            ResourceString(Res.string.auth_error_sign_up_failed)
    }

    @Test
    fun When_sign_up_succeeds_with_valid_password_Then_no_password_error_and_repo_called() =
        runTest {
            val repo = FakeAuthenticationRepository()
            repo.signUpResult = Result.success(SignUpResult.AwaitingEmailVerification)
            val viewModel = viewModel(repo = repo)

            viewModel.onEmailChanged("user@example.com")
            viewModel.onPasswordChanged("Abc1234!")
            viewModel.onConfirmPasswordChanged("Abc1234!")
            viewModel.onSubmitClicked()

            viewModel.state.value.passwordError shouldBe null
            viewModel.state.value.errorMessage shouldBe null
        }
}
