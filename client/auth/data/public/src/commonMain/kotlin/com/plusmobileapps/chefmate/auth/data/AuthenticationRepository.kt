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

    /**
     * Returned when a currently-anonymous user is upgraded to a real account via
     * [AuthenticationRepository.signUpWithEmailAndPassword]. Supabase delivers a Change Email
     * Address confirmation rather than a fresh Confirm Signup, so the caller must route to OTP
     * verification with [OtpFlow.EmailChange] (not [OtpFlow.SignUp]) so the verify call uses the
     * matching token type.
     */
    data object AwaitingEmailChange : SignUpResult()

    data object UserAlreadyExists : SignUpResult()
}

@Serializable
enum class OtpFlow {
    SignUp,
    PasswordlessSignIn,

    /**
     * Email-change confirmation used during anon-to-real account upgrades. Maps to
     * `OtpType.Email.EMAIL_CHANGE` on the Supabase side so the verify call matches the token
     * Supabase actually issued when [AuthenticationRepository.signUpWithEmailAndPassword] triggered
     * `updateUser { email; password }`.
     */
    EmailChange,
}
