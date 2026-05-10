@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.auth.ui.impl.otp

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest

class OtpViewModelTest {

    private fun TestScope.viewModel(
        repo: FakeAuthenticationRepository = FakeAuthenticationRepository(),
        flow: OtpFlow = OtpFlow.SignUp,
        email: String = "user@example.com",
    ): OtpViewModel =
        OtpViewModel(
            props = OtpBloc.Props(email = email, flow = flow),
            mainContext = UnconfinedTestDispatcher(testScheduler),
            authRepository = repo,
        )

    @Test
    fun When_verify_clicked_with_blank_code_Then_required_error_set_and_no_repo_call() = runTest {
        val repo = FakeAuthenticationRepository()
        val viewModel = viewModel(repo = repo)

        viewModel.onVerifyClicked()

        viewModel.state.value.errorMessage shouldNotBe null
        viewModel.state.value.isLoading shouldBe false
        repo.lastVerifyOtpFlow shouldBe null
    }

    @Test
    fun When_verify_clicked_with_short_code_Then_invalid_length_error_set() = runTest {
        val repo = FakeAuthenticationRepository()
        val viewModel = viewModel(repo = repo)

        viewModel.onCodeChanged("123")
        viewModel.onVerifyClicked()

        viewModel.state.value.errorMessage shouldNotBe null
        repo.lastVerifyOtpFlow shouldBe null
    }

    @Test
    fun When_code_contains_letters_Then_only_digits_kept_and_length_capped() = runTest {
        val viewModel = viewModel()

        viewModel.onCodeChanged("12abc34d56789")

        viewModel.code.value shouldBe "12345678"
    }

    @Test
    fun When_verify_clicked_with_valid_code_Then_repo_called_with_signup_flow_and_verified_emitted() =
        runTest {
            val repo = FakeAuthenticationRepository()
            val viewModel = viewModel(repo = repo)

            viewModel.outputs.test {
                viewModel.onCodeChanged("12345678")
                viewModel.onVerifyClicked()

                awaitItem() shouldBe OtpViewModel.Output.Verified
                cancelAndIgnoreRemainingEvents()
            }

            repo.lastVerifyOtpFlow shouldBe OtpFlow.SignUp
            viewModel.state.value.isLoading shouldBe false
        }

    @Test
    fun Given_passwordless_flow_When_verify_clicked_Then_repo_called_with_passwordless_flow() =
        runTest {
            val repo = FakeAuthenticationRepository()
            val viewModel = viewModel(repo = repo, flow = OtpFlow.PasswordlessSignIn)

            viewModel.outputs.test {
                viewModel.onCodeChanged("87654321")
                viewModel.onVerifyClicked()

                awaitItem() shouldBe OtpViewModel.Output.Verified
                cancelAndIgnoreRemainingEvents()
            }

            repo.lastVerifyOtpFlow shouldBe OtpFlow.PasswordlessSignIn
        }

    @Test
    fun Given_repo_returns_expired_failure_When_verify_clicked_Then_expired_error_set() = runTest {
        val repo =
            FakeAuthenticationRepository().apply {
                verifyEmailOtpResult = Result.failure(IllegalStateException("Token has expired"))
            }
        val viewModel = viewModel(repo = repo)

        viewModel.onCodeChanged("12345678")
        viewModel.onVerifyClicked()

        viewModel.state.value.errorMessage shouldNotBe null
        viewModel.state.value.isLoading shouldBe false
    }

    @Test
    fun When_resend_clicked_Then_repo_called_and_countdown_starts() = runTest {
        val repo = FakeAuthenticationRepository()
        val viewModel = viewModel(repo = repo)

        viewModel.onResendClicked()

        repo.lastResendOtpFlow shouldBe OtpFlow.SignUp
        viewModel.state.value.resendCountdownSeconds shouldBe OtpViewModel.RESEND_COUNTDOWN_SECONDS
        viewModel.state.value.infoMessage shouldNotBe null
    }

    @Test
    fun Given_resend_in_progress_When_time_advances_Then_countdown_ticks_to_zero() = runTest {
        val repo = FakeAuthenticationRepository()
        val viewModel = viewModel(repo = repo)

        viewModel.onResendClicked()
        viewModel.state.value.resendCountdownSeconds shouldBe OtpViewModel.RESEND_COUNTDOWN_SECONDS

        advanceTimeBy(OtpViewModel.RESEND_COUNTDOWN_SECONDS * 1_000L + 100L)

        viewModel.state.value.resendCountdownSeconds shouldBe 0
    }
}
