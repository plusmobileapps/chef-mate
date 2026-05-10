package com.plusmobileapps.chefmate.devsettings.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.Environment
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc.Output
import com.plusmobileapps.chefmate.devsettings.TestUser
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
class DeveloperSettingsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<DeveloperSettingsViewModel>,
) : DeveloperSettingsBloc, BlocContext by context {

    @AssistedFactory
    fun interface ManagedFactory {
        fun create(context: BlocContext, output: Consumer<Output>): DeveloperSettingsBlocImpl
    }

    private val viewModel = instanceKeeper.getViewModel { viewModelFactory() }

    override val state: StateFlow<DeveloperSettingsBloc.Model> = viewModel.state

    override fun onBack() {
        output.onNext(Output.Back)
    }

    override fun onEnvironmentClicked() {
        viewModel.showEnvironmentPicker()
    }

    override fun onEnvironmentSelected(environment: Environment) {
        viewModel.changeEnvironment(environment)
    }

    override fun onEnvironmentPickerDismissed() {
        viewModel.dismissEnvironmentPicker()
    }

    override fun onLoginAsUserClicked() {
        viewModel.showUserPicker()
    }

    override fun onUserSelected(user: TestUser) {
        viewModel.signInAsTestUser(user)
    }

    override fun onUserPickerDismissed() {
        viewModel.dismissUserPicker()
    }

    override fun onSignOutTestUserClicked() {
        viewModel.signOutTestUser()
    }

    override fun onRestartPromptDismissed() {
        viewModel.dismissRestartPrompt()
    }

    override fun onSignInErrorDismissed() {
        viewModel.dismissSignInError()
    }

    override fun onFeatureFlagsClicked() {
        output.onNext(Output.OpenFeatureFlags)
    }
}

@ContributesTo(AppScope::class)
interface DeveloperSettingsBlocBindingModule {
    @Provides
    fun provideDeveloperSettingsBlocFactory(
        factory: DeveloperSettingsBlocImpl.ManagedFactory
    ): DeveloperSettingsBloc.Factory = DeveloperSettingsBloc.Factory { context, output ->
        factory.create(context, output)
    }
}
