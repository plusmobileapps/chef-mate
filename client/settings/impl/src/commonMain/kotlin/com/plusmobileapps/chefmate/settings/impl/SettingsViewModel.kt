package com.plusmobileapps.chefmate.settings.impl

import com.plusmobileapps.chefmate.ViewModel
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.di.Main
import com.plusmobileapps.chefmate.grocery.data.GroceryRepository
import com.plusmobileapps.chefmate.recipe.data.RecipeRepository
import dev.zacsweers.metro.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Inject
class SettingsViewModel(
    @Main mainContext: CoroutineContext,
    private val authenticationRepository: AuthenticationRepository,
    private val groceryRepository: GroceryRepository,
    private val recipeRepository: RecipeRepository,
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
                        val displayName =
                            authState.user.userName.ifBlank { authState.user.userEmail }
                        _state.value =
                            State(
                                isAuthenticated = true,
                                userName = displayName,
                                emailAwaitingVerification = null,
                            )
                    }
                    is AuthState.Unauthenticated -> {
                        _state.value =
                            State(
                                isAuthenticated = false,
                                userName = null,
                                emailAwaitingVerification = null,
                            )
                    }
                    is AuthState.AwaitingEmailVerification -> {
                        _state.value =
                            State(
                                isAuthenticated = false,
                                userName = null,
                                emailAwaitingVerification = authState.email,
                            )
                    }
                }
            }
            .launchIn(scope)
    }

    fun signOut() {
        scope.launch {
            authenticationRepository.signOut()
            groceryRepository.clearLocalData()
            recipeRepository.clearLocalData()
            groceryRepository.ensureDefaultList()
        }
    }

    data class State(
        val isAuthenticated: Boolean = false,
        val userName: String? = null,
        val emailAwaitingVerification: String? = null,
    )
}
