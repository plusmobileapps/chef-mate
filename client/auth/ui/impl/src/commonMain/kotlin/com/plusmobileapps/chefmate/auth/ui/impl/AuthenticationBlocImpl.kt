package com.plusmobileapps.chefmate.auth.ui.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc.Output
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AuthenticationBloc.Factory::class,
)
class AuthenticationBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: AuthenticationBloc.Props,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: AuthenticationViewModel.Factory,
) : AuthenticationBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel: AuthenticationViewModel = instanceKeeper.getViewModel {
        viewModelFactory.create(props)
    }

    override val models: StateFlow<AuthenticationBloc.Model> =
        viewModel.state.mapState {
            AuthenticationBloc.Model(
                mode = it.mode,
                isLoading = it.isLoading,
                errorMessage = it.errorMessage,
                emailError = it.emailError,
                passwordError = it.passwordError,
                confirmPasswordError = it.confirmPasswordError,
                pendingGuestDataDiscard = it.pendingGuestDataDiscard,
            )
        }

    override val email: StateFlow<String> = viewModel.email
    override val password: StateFlow<String> = viewModel.password
    override val confirmPassword: StateFlow<String> = viewModel.confirmPassword

    init {
        scope.launch {
            viewModel.outputs.collect {
                when (it) {
                    AuthenticationViewModel.Output.AuthenticationSuccess ->
                        output.onNext(Output.AuthenticationSuccess)
                    is AuthenticationViewModel.Output.EmailVerificationRequired ->
                        output.onNext(Output.EmailVerificationRequired(it.email))
                    is AuthenticationViewModel.Output.EmailChangeRequired ->
                        output.onNext(Output.EmailChangeRequired(it.email))
                    is AuthenticationViewModel.Output.PasswordlessOtpSent ->
                        output.onNext(Output.PasswordlessOtpSent(it.email))
                    is AuthenticationViewModel.Output.OpenUrl ->
                        output.onNext(Output.OpenUrl(it.url))
                }
            }
        }
    }

    override fun onEmailChanged(email: String) {
        viewModel.onEmailChanged(email)
    }

    override fun onPasswordChanged(password: String) {
        viewModel.onPasswordChanged(password)
    }

    override fun onConfirmPasswordChanged(confirmPassword: String) {
        viewModel.onConfirmPasswordChanged(confirmPassword)
    }

    override fun onSubmitClicked() {
        viewModel.onSubmitClicked()
    }

    override fun onToggleMode() {
        viewModel.onToggleMode()
    }

    override fun onForgotPasswordClicked() {
        viewModel.forgotPassword()
    }

    override fun onEmailMeACodeClicked() {
        viewModel.onEmailMeACodeClicked()
    }

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }

    override fun onUrlClicked(url: String) {
        viewModel.onUrlClicked(url)
    }

    override fun onDismissError() {
        viewModel.onDismissError()
    }

    override fun onDiscardGuestDataConfirmed() {
        viewModel.onDiscardGuestDataConfirmed()
    }

    override fun onDiscardGuestDataCancelled() {
        viewModel.onDiscardGuestDataCancelled()
    }
}
