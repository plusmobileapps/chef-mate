package com.plusmobileapps.chefmate.devsettings.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.Environment
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc.Output
import com.plusmobileapps.chefmate.devsettings.TestUser
import com.plusmobileapps.chefmate.devsettings.impl.ui.DeveloperSettingsScreen
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.getViewModel
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider
import kotlinx.coroutines.flow.StateFlow

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = DeveloperSettingsBloc.Factory::class,
)
class DeveloperSettingsBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    viewModelFactory: Provider<DeveloperSettingsViewModel>,
) : DeveloperSettingsBloc, BlocContext by context {

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

    @Composable
    override fun Content(modifier: Modifier) {
        DeveloperSettingsScreen(bloc = this, modifier = modifier)
    }
}
