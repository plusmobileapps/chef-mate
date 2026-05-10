@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.auth.ui.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.GoogleSignInOutcome
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.util.EmailUtil
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class AuthenticationViewModelGoogleTest {

    private fun TestScope.viewModel(
        repo: FakeAuthenticationRepository = FakeAuthenticationRepository()
    ): AuthenticationViewModel =
        AuthenticationViewModel(
            initialProps = AuthenticationBloc.Props.SignIn,
            mainContext = UnconfinedTestDispatcher(testScheduler),
            authRepository = repo,
            emailUtil = AlwaysValidEmailUtil,
        )

    @Test
    fun Given_repo_succeeds_When_google_clicked_Then_authentication_success_emitted() = runTest {
        val repo =
            FakeAuthenticationRepository().apply {
                signInWithGoogleResult = Result.success(GoogleSignInOutcome.Success)
            }
        val viewModel = viewModel(repo = repo)

        viewModel.outputs.test {
            viewModel.onGoogleSignInClicked()

            awaitItem() shouldBe AuthenticationViewModel.Output.AuthenticationSuccess
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.state.value.isLoading shouldBe false
    }

    @Test
    fun Given_repo_returns_cancelled_When_google_clicked_Then_loading_cleared_with_no_output() =
        runTest {
            val repo =
                FakeAuthenticationRepository().apply {
                    signInWithGoogleResult = Result.success(GoogleSignInOutcome.Cancelled)
                }
            val viewModel = viewModel(repo = repo)

            viewModel.outputs.test {
                viewModel.onGoogleSignInClicked()
                expectNoEvents()
            }

            viewModel.state.value.isLoading shouldBe false
            viewModel.state.value.errorMessage shouldBe null
        }

    @Test
    fun Given_repo_fails_When_google_clicked_Then_loading_cleared_and_error_set() = runTest {
        val repo =
            FakeAuthenticationRepository().apply {
                signInWithGoogleResult = Result.failure(IllegalStateException("kaboom"))
            }
        val viewModel = viewModel(repo = repo)

        viewModel.onGoogleSignInClicked()

        viewModel.state.value.isLoading shouldBe false
        viewModel.state.value.errorMessage shouldNotBe null
    }

    private object AlwaysValidEmailUtil : EmailUtil {
        override fun isValidEmail(email: String): Boolean = true
    }
}
