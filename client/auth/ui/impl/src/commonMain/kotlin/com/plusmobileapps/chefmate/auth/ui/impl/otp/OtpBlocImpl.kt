package com.plusmobileapps.chefmate.auth.ui.impl.otp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.ui.impl.otp.ui.OtpScreen
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc.Output
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = OtpBloc.Factory::class)
class OtpBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: OtpBloc.Props,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: OtpViewModel.Factory,
) : OtpBloc, BlocContext by context {

    private val scope = createScope()

    private val viewModel: OtpViewModel = instanceKeeper.getViewModel {
        viewModelFactory.create(props)
    }

    override val models: StateFlow<OtpBloc.Model> =
        viewModel.state.mapState {
            OtpBloc.Model(
                email = it.email,
                flow = it.flow,
                isLoading = it.isLoading,
                errorMessage = it.errorMessage,
                infoMessage = it.infoMessage,
                resendCountdownSeconds = it.resendCountdownSeconds,
            )
        }

    override val code: StateFlow<String> = viewModel.code

    init {
        scope.launch {
            viewModel.outputs.collect {
                when (it) {
                    OtpViewModel.Output.Verified -> output.onNext(Output.Verified)
                }
            }
        }
    }

    override fun onCodeChanged(code: String) {
        viewModel.onCodeChanged(code)
    }

    override fun onVerifyClicked() {
        viewModel.onVerifyClicked()
    }

    override fun onResendClicked() {
        viewModel.onResendClicked()
    }

    override fun onDismissError() {
        viewModel.onDismissError()
    }

    override fun onBackClicked() {
        output.onNext(Output.Cancelled)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        OtpScreen(bloc = this, modifier = modifier)
    }
}
