package com.plusmobileapps.chefmate.auth.ui.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc.Output
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
class AuthenticationBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: AuthenticationBloc.Props,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: AuthenticationViewModel.Factory,
) : AuthenticationBloc,
    BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            props: AuthenticationBloc.Props,
            output: Consumer<Output>,
        ): AuthenticationBlocImpl
    }

    private val scope = createScope()

    private val viewModel: AuthenticationViewModel =
        instanceKeeper.getViewModel {
            viewModelFactory.create(props)
        }

    override val models: StateFlow<AuthenticationBloc.Model> =
        viewModel.state.mapState {
            AuthenticationBloc.Model(
                mode = it.mode,
                isLoading = it.isLoading,
                errorMessage = it.errorMessage,
                emailError = it.emailError,
                confirmPasswordError = it.confirmPasswordError,
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

    override fun onBackClicked() {
        output.onNext(Output.Finished)
    }

    override fun onDismissError() {
        viewModel.onDismissError()
    }
}

@ContributesTo(AppScope::class)
interface AuthenticationBlocBindingModule {
    @Provides
    fun provideAuthenticationBlocFactory(factory: AuthenticationBlocImpl.ManagedFactory): AuthenticationBloc.Factory =
        AuthenticationBloc.Factory { context, props, output -> factory.create(context, props, output) }
}
