package com.plusmobileapps.chefmate.auth.ui.impl.otp

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc
import com.plusmobileapps.chefmate.auth.ui.otp.OtpBloc.Output
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@AssistedInject
class OtpBlocImpl(
    @Assisted context: BlocContext,
    @Assisted props: OtpBloc.Props,
    @Assisted private val output: Consumer<Output>,
    private val viewModelFactory: OtpViewModel.Factory,
) : OtpBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(
            context: BlocContext,
            props: OtpBloc.Props,
            output: Consumer<Output>,
        ): OtpBlocImpl
    }

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
}

@ContributesTo(AppScope::class)
interface OtpBlocBindingModule {
    @Provides
    fun provideOtpBlocFactory(factory: OtpBlocImpl.ManagedFactory): OtpBloc.Factory =
        OtpBloc.Factory { context, props, output ->
            factory.create(context, props, output)
        }
}
