package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.usecase.SignOutUseCase
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
class SettingsViewModel(
    @Main mainContext: CoroutineContext,
    private val authenticationRepository: AuthenticationRepository,
    private val signOutUseCase: SignOutUseCase,
) : ViewModel(mainContext) {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        observeAuthState()
    }

    private fun observeAuthState() {
        authenticationRepository.state
            .onEach { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        val isAnonymous = authState.user.isAnonymous
                        val displayName =
                            if (isAnonymous) null
                            else authState.user.userName.ifBlank { authState.user.userEmail }
                        _state.value =
                            State(
                                isAuthenticated = true,
                                isAnonymous = isAnonymous,
                                userName = displayName,
                                emailAwaitingVerification = null,
                            )
                    }
                    is AuthState.Unauthenticated -> {
                        _state.value =
                            State(
                                isAuthenticated = false,
                                isAnonymous = false,
                                userName = null,
                                emailAwaitingVerification = null,
                            )
                    }
                    is AuthState.AwaitingEmailVerification -> {
                        _state.value =
                            State(
                                isAuthenticated = false,
                                isAnonymous = false,
                                userName = null,
                                emailAwaitingVerification = authState.email,
                            )
                    }
                }
            }
            .launchIn(scope)
    }

    fun showSignOutConfirmationDialog() {
        _state.update { it.copy(showSignOutConfirmationDialog = true) }
    }

    fun dismissSignOutConfirmationDialog() {
        _state.update { it.copy(showSignOutConfirmationDialog = false) }
    }

    fun signOut() {
        _state.update { it.copy(showSignOutConfirmationDialog = false) }
        scope.launch { signOutUseCase() }
    }

    data class State(
        val isAuthenticated: Boolean = false,
        val isAnonymous: Boolean = false,
        val userName: String? = null,
        val emailAwaitingVerification: String? = null,
        val showSignOutConfirmationDialog: Boolean = false,
    )
}
