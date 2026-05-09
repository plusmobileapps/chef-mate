package com.plusmobileapps.chefmate.devsettings

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.Environment
import kotlinx.coroutines.flow.StateFlow

interface DeveloperSettingsBloc {
    val state: StateFlow<Model>

    fun onBack()

    fun onEnvironmentClicked()

    fun onEnvironmentSelected(environment: Environment)

    fun onEnvironmentPickerDismissed()

    fun onLoginAsUserClicked()

    fun onUserSelected(user: TestUser)

    fun onUserPickerDismissed()

    fun onSignOutTestUserClicked()

    fun onRestartPromptDismissed()

    fun onSignInErrorDismissed()

    fun onFeatureFlagsClicked()

    data class Model(
        val currentEnvironment: Environment = Environment.PROD,
        val availableUsers: List<TestUser> = emptyList(),
        val selectedUserIndex: Int? = null,
        val currentUserEmail: String? = null,
        val isAuthenticated: Boolean = false,
        val showEnvironmentPicker: Boolean = false,
        val showUserPicker: Boolean = false,
        val showRestartPrompt: Boolean = false,
        val signInError: String? = null,
    )

    sealed class Output {
        data object Back : Output()

        data object OpenFeatureFlags : Output()
    }

    fun interface Factory {
        fun create(context: BlocContext, output: Consumer<Output>): DeveloperSettingsBloc
    }
}
