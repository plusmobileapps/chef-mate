@file:Suppress("FunctionName")
@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plusmobileapps.chefmate.settings.impl

import app.cash.turbine.test
import com.plusmobileapps.chefmate.auth.data.testing.FakeAuthenticationRepository
import com.plusmobileapps.chefmate.auth.usecase.SignOutUseCase
import com.plusmobileapps.chefmate.settings.SettingsBloc
import com.plusmobileapps.chefmate.testing.TestBlocContext
import com.plusmobileapps.chefmate.testing.TestConsumer
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest

class SettingsBlocImplTest {

    private val context = TestBlocContext.create()
    private val output = TestConsumer<SettingsBloc.Output>()
    private val authRepository = FakeAuthenticationRepository()
    private val signOutUseCase = SignOutUseCase { authRepository.signOut() }

    private val bloc by lazy {
        SettingsBlocImpl(
            context = context,
            output = output,
            viewModelFactory = {
                SettingsViewModel(
                    mainContext = context.mainContext,
                    authenticationRepository = authRepository,
                    signOutUseCase = signOutUseCase,
                )
            },
        )
    }

    @Test
    fun When_sign_out_clicked_Then_confirmation_dialog_is_shown() = runTest {
        authRepository.setAuthenticated()

        bloc.state.test {
            awaitItem() // initial authenticated state

            bloc.onSignOutClicked()

            awaitItem().showSignOutConfirmationDialog shouldBe true
        }
    }

    @Test
    fun When_sign_out_dismissed_Then_confirmation_dialog_is_hidden() = runTest {
        authRepository.setAuthenticated()

        bloc.state.test {
            awaitItem()

            bloc.onSignOutClicked()
            awaitItem().showSignOutConfirmationDialog shouldBe true

            bloc.onSignOutDismissed()
            awaitItem().showSignOutConfirmationDialog shouldBe false
        }
    }

    @Test
    fun When_sign_out_confirmed_Then_user_is_signed_out() = runTest {
        authRepository.setAuthenticated()
        bloc.onSignOutClicked()
        bloc.onSignOutConfirmed()

        bloc.state.value.isAuthenticated shouldBe false
        bloc.state.value.showSignOutConfirmationDialog shouldBe false
    }

    @Test
    fun When_sign_out_confirmed_Then_dialog_is_hidden() = runTest {
        authRepository.setAuthenticated()

        bloc.state.test {
            awaitItem()
            bloc.onSignOutClicked()
            awaitItem()

            bloc.onSignOutConfirmed()
            awaitItem().showSignOutConfirmationDialog shouldBe false
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun When_sign_in_clicked_Then_OpenSignIn_output_emitted() {
        bloc.onSignInClicked()
        output.lastValue shouldBe SettingsBloc.Output.OpenSignIn
    }

    @Test
    fun When_sign_up_clicked_Then_OpenSignUp_output_emitted() {
        bloc.onSignUpClicked()
        output.lastValue shouldBe SettingsBloc.Output.OpenSignUp
    }

    @Test
    fun When_url_clicked_Then_OpenUrl_output_emitted() {
        val url = "https://example.com"
        bloc.onUrlClicked(url)
        output.lastValue shouldBe SettingsBloc.Output.OpenUrl(url)
    }

    @Test
    fun When_app_settings_clicked_Then_OpenSettingsRoot_output_emitted() {
        bloc.onAppSettingsClicked()
        output.lastValue shouldBe SettingsBloc.Output.OpenSettingsRoot
    }

    @Test
    fun When_authenticated_Then_state_reflects_authenticated() = runTest {
        bloc.state.test {
            awaitItem().isAuthenticated shouldBe false

            authRepository.setAuthenticated()
            awaitItem().isAuthenticated shouldBe true
        }
    }
}
