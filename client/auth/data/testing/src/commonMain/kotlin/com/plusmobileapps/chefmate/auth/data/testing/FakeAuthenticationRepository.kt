package com.plusmobileapps.chefmate.auth.data.testing

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthenticationRepository : AuthenticationRepository {
    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state

    fun setState(state: AuthState) {
        _state.value = state
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) =
        Result.success(Unit)

    override suspend fun signUpWithEmailAndPassword(email: String, password: String) =
        Result.success(SignUpResult.Success as SignUpResult)

    override suspend fun signOut() {
        _state.value = AuthState.Unauthenticated
    }

    override suspend fun sendPasswordResetEmail(email: String) = Result.success(Unit)
}
