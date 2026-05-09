package com.plusmobileapps.chefmate.devsettings.impl

import com.plusmobileapps.chefmate.Environment
import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.usecase.SignOutUseCase
import com.plusmobileapps.chefmate.devsettings.DeveloperPreferences
import com.plusmobileapps.chefmate.devsettings.DeveloperSettingsBloc
import com.plusmobileapps.chefmate.devsettings.TestUser
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class DeveloperSettingsViewModel(
    @Main mainContext: CoroutineContext,
    private val preferences: DeveloperPreferences,
    private val testUserProvider: TestUserProvider,
    private val authenticationRepository: AuthenticationRepository,
    private val signOutUseCase: SignOutUseCase,
    private val fakeRecipeSeeder: FakeRecipeSeeder,
) : ViewModel(mainContext) {

    private val _state =
        MutableStateFlow(DeveloperSettingsBloc.Model(availableUsers = testUserProvider.users))
    val state: StateFlow<DeveloperSettingsBloc.Model> = _state.asStateFlow()

    init {
        combine(
                preferences.environment,
                preferences.selectedUserIndex,
                authenticationRepository.state,
            ) { env, userIndex, authState ->
                Triple(env, userIndex, authState)
            }
            .onEach { (env, userIndex, authState) ->
                _state.update {
                    it.copy(
                        currentEnvironment = env,
                        selectedUserIndex = userIndex,
                        currentUserEmail = (authState as? AuthState.Authenticated)?.user?.userEmail,
                        isAuthenticated = authState is AuthState.Authenticated,
                    )
                }
            }
            .launchIn(scope)
    }

    fun showEnvironmentPicker() {
        _state.update { it.copy(showEnvironmentPicker = true) }
    }

    fun dismissEnvironmentPicker() {
        _state.update { it.copy(showEnvironmentPicker = false) }
    }

    fun showUserPicker() {
        _state.update { it.copy(showUserPicker = true) }
    }

    fun dismissUserPicker() {
        _state.update { it.copy(showUserPicker = false) }
    }

    fun dismissRestartPrompt() {
        _state.update { it.copy(showRestartPrompt = false) }
    }

    fun dismissSignInError() {
        _state.update { it.copy(signInError = null) }
    }

    fun changeEnvironment(environment: Environment) {
        _state.update { it.copy(showEnvironmentPicker = false) }
        if (environment == _state.value.currentEnvironment) return
        scope.launch {
            preferences.setEnvironment(environment)
            preferences.setSelectedUserIndex(null)
            signOutUseCase()
            if (environment == Environment.FAKE) {
                fakeRecipeSeeder.seed()
            }
            _state.update { it.copy(showRestartPrompt = true) }
        }
    }

    fun signInAsTestUser(user: TestUser) {
        _state.update { it.copy(showUserPicker = false, signInError = null) }
        scope.launch {
            authenticationRepository.signOut()
            val result =
                authenticationRepository.signInWithEmailAndPassword(user.email, user.password)
            if (result.isSuccess) {
                preferences.setSelectedUserIndex(user.index)
            } else {
                val message =
                    result.exceptionOrNull()?.message?.takeIf(String::isNotBlank).orEmpty()
                _state.update { it.copy(signInError = message) }
            }
        }
    }

    fun signOutTestUser() {
        scope.launch {
            preferences.setSelectedUserIndex(null)
            signOutUseCase()
        }
    }
}
