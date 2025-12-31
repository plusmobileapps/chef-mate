package com.plusmobileapps.chefmate.auth.ui.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc
import com.plusmobileapps.chefmate.auth.ui.AuthenticationBloc.Output
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AuthenticationBloc.Factory::class,
)
class AuthenticationBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: AuthenticationBloc.Props,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: (AuthenticationBloc.Props) -> AuthenticationViewModel,
) : AuthenticationBloc,
    BlocContext by context {
    private val scope = createScope()

    private val viewModel: AuthenticationViewModel =
        instanceKeeper.getViewModel {
            viewModelFactory(props)
        }

    override val models: StateFlow<AuthenticationBloc.Model> =
        viewModel.state.mapState {
            AuthenticationBloc.Model(
                mode = it.mode,
                isLoading = it.isLoading,
                errorMessage = it.errorMessage,
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
