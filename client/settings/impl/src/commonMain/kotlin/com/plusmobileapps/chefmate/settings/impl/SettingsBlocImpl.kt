package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.chefmate.mapState
import com.plusmobileapps.chefmate.settings.SettingsBloc
import com.plusmobileapps.chefmate.settings.SettingsBloc.Output
import com.plusmobileapps.chefmate.settings.createEmailVerificationMessage
import com.plusmobileapps.chefmate.settings.createGreeting
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class SettingsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<SettingsViewModel>,
) : SettingsBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): SettingsBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<SettingsBloc.Model> =
        viewModel.state.mapState {
            SettingsBloc.Model(
                isAuthenticated = it.isAuthenticated,
                greeting = it.userName?.let { name -> createGreeting(name) },
                verificationMessage =
                    it.emailAwaitingVerification?.let { email ->
                        createEmailVerificationMessage(email)
                    },
                showSignOutConfirmationDialog = it.showSignOutConfirmationDialog,
            )
        }

    override fun onSignInClicked() {
        output.onNext(Output.OpenSignIn)
    }

    override fun onSignUpClicked() {
        output.onNext(Output.OpenSignUp)
    }

    override fun onSignOutClicked() {
        viewModel.showSignOutConfirmationDialog()
    }

    override fun onSignOutConfirmed() {
        viewModel.signOut()
    }

    override fun onSignOutDismissed() {
        viewModel.dismissSignOutConfirmationDialog()
    }

    override fun onUrlClicked(url: String) {
        output.onNext(Output.OpenUrl(url))
    }
}

@ContributesTo(AppScope::class)
interface SettingsBlocBindingModule {
    @Provides
    fun provideSettingsBlocFactory(factory: SettingsBlocImpl.ManagedFactory): SettingsBloc.Factory =
        SettingsBloc.Factory { context, output ->
            factory.create(context, output)
        }
}
