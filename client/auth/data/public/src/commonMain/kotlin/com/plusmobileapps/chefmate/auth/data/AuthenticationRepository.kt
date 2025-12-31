package com.plusmobileapps.chefmate.auth.data

import kotlinx.coroutines.flow.StateFlow

interface AuthenticationRepository {
    val state: StateFlow<AuthState>

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<ChefMateUser>

    suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<ChefMateUser>

    suspend fun signOut()

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
}
