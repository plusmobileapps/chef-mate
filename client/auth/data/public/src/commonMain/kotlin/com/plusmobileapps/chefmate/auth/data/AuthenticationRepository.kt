package com.plusmobileapps.chefmate.auth.data

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

interface AuthenticationRepository {
    val state: StateFlow<AuthState>

    /**
     * Idempotently ensures a Supabase session exists. Returns immediately if already authenticated
     * (anonymous or real). Otherwise lazily signs in anonymously — that's the only path callers
     * have to materialize a session without going through email/password sign-in or sign-up.
     *
     * Callers should invoke this right before any operation that requires `auth.uid()` (currently
     * only photo uploads). Pure-local operations — saving a text recipe, adding a grocery item —
     * must not call this, so users who never trigger a remote operation never appear as anonymous
     * Supabase users (and never count toward MAU).
     */
    suspend fun ensureSession(): Result<Unit>

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit>

    suspend fun signUpWithEmailAndPassword(email: String, password: String): Result<SignUpResult>

    suspend fun signOut()

    /**
     * Updates the signed-in user's profile metadata. [avatarUrl] is only written when non-null, so
     * passing `null` leaves an existing avatar untouched. The Supabase session collector mirrors
     * the updated metadata back into [state], so callers don't need to refresh manually.
     */
    suspend fun updateProfile(displayName: String, avatarUrl: String?): Result<Unit>

    /**
     * Permanently deletes the signed-in user's remote account. The actual deletion runs in a
     * service-role Supabase Edge Function (`delete-account`) because the client SDK can't delete
     * auth users. Callers are responsible for the subsequent sign-out + local-data wipe (see
     * `DeleteAccountUseCase`).
     */
    suspend fun deleteAccount(): Result<Unit>

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
