package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.settings.SettingsBloc
import com.plusmobileapps.chefmate.settings.SettingsBloc.Output
import com.plusmobileapps.chefmate.settings.createEmailVerificationMessage
import com.plusmobileapps.chefmate.settings.createGreeting
import com.plusmobileapps.kotlin.inject.anvil.extensions.assistedfactory.runtime.ContributesAssistedFactory
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope

@Inject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = SettingsBloc.Factory::class,
)
class SettingsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: () -> SettingsViewModel,
) : SettingsBloc,
    BlocContext by context {
    private val viewModel =
        instanceKeeper.getViewModel {
            viewModelFactory()
        }

    override val state: StateFlow<SettingsBloc.Model> =
        viewModel.state.mapState {
            SettingsBloc.Model(
                isAuthenticated = it.isAuthenticated,
                greeting = it.userName?.let { name -> createGreeting(name) },
                verificationMessage =
                    it.emailAwaitingVerification?.let { email ->
                        createEmailVerificationMessage(email)
                    },
            )
        }

    override fun onSignInClicked() {
        output.onNext(Output.OpenSignIn)
    }

    override fun onSignUpClicked() {
        output.onNext(Output.OpenSignUp)
    }

    override fun onSignOutClicked() {
        viewModel.signOut()
    }
}
