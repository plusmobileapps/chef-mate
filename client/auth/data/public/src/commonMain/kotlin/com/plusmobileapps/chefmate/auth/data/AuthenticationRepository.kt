package com.plusmobileapps.chefmate.auth.data

import kotlinx.coroutines.flow.StateFlow

interface AuthenticationRepository {
    val state: StateFlow<AuthState>

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<Unit>

    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<SignUpResult>

    suspend fun signOut()

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}

sealed class SignUpResult {
    data object Success : SignUpResult()

    data object AwaitingEmailVerification : SignUpResult()
}
