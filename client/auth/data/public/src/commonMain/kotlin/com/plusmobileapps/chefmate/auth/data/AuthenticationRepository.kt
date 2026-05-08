package com.plusmobileapps.chefmate.auth.data

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AuthenticationRepository {
    val state: StateFlow<AuthState>

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>

    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<SignUpResult>

    suspend fun signOut()

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun sendSignInOtp(email: String): Result<Unit>

    suspend fun verifyEmailOtp(email: String, token: String, flow: OtpFlow): Result<Unit>

    suspend fun resendOtp(email: String, flow: OtpFlow): Result<Unit>
}

sealed class SignUpResult {
    data object Success : SignUpResult()

    data object AwaitingEmailVerification : SignUpResult()

    data object UserAlreadyExists : SignUpResult()
}

@Serializable
enum class OtpFlow {
    SignUp,
    PasswordlessSignIn,
}
