package com.plusmobileapps.chefmate.auth.data.impl

import co.touchlab.kermit.Logger
import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.data.SessionTokens
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.Main
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.functions.functions
import kotlin.coroutines.CoroutineContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class SupabaseAuthenticationRepository(
    private val supabaseClient: SupabaseClient,
    @Main private val mainContext: CoroutineContext,
) : AuthenticationRepository {
    private val scope = CoroutineScope(mainContext)

    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Mirror the Supabase session state into our AuthState. The NotAuthenticated branch no
        // longer auto-bootstraps an anonymous session — callers that need a session call
        // [ensureSession] lazily (currently only the photo upload path). Users who only ever
        // create text recipes / grocery items never trigger a Supabase auth row and so never
        // count toward MAU.
        scope.launch {
            supabaseClient.auth.sessionStatus.collect { sessionStatus ->
                when (sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        val user = sessionStatus.session.user
                        val isAnon = isAnonymousJwt(sessionStatus.session.accessToken)
                        user?.let {
                            _state.value =
                                AuthState.Authenticated(it.toChefMateUser(isAnonymous = isAnon))
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        if (_state.value !is AuthState.AwaitingEmailVerification) {
                            _state.value = AuthState.Unauthenticated
                        }
                    }
                    is SessionStatus.Initializing,
                    is SessionStatus.RefreshFailure -> {
                        // Keep current state during initialization or refresh failures
                    }
                }
            }
        }
    }

    override suspend fun currentSessionTokens(): SessionTokens? =
        supabaseClient.auth.currentSessionOrNull()?.let { session ->
            SessionTokens(
                accessToken = session.accessToken,
                expiresAtEpochSeconds = session.expiresAt.epochSeconds,
            )
        }

    override suspend fun ensureSession(): Result<Unit> {
        if (_state.value is AuthState.Authenticated) return Result.success(Unit)
        return try {
            supabaseClient.auth.signInAnonymously()
            // sessionStatus collector above flips us into Authenticated.
            Result.success(Unit)
        } catch (t: Throwable) {
            Logger.w(throwable = t, tag = TAG) { "Anonymous sign-in failed" }
            Result.failure(t)
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> =
        try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<SignUpResult> {
        return try {
            // If we're currently signed in anonymously, upgrade the existing account in-place via
            // updateUser{} so the auth.uid() — and therefore every recipe/photo already attached
            // to it — is preserved. Supabase still sends a confirmation email; the JWT loses
            // is_anonymous after the user confirms.
            val currentSession = supabaseClient.auth.currentSessionOrNull()
            if (currentSession != null && isAnonymousJwt(currentSession.accessToken)) {
                supabaseClient.auth.updateUser {
                    this.email = email
                    this.password = password
                }
                _state.value = AuthState.AwaitingEmailVerification(email)
                // Distinct from AwaitingEmailVerification: updateUser{} triggers Supabase's
                // Change Email Address template, so verification must route through
                // OtpFlow.EmailChange → OtpType.Email.EMAIL_CHANGE.
                return Result.success(SignUpResult.AwaitingEmailChange)
            }

            val result =
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

            // Supabase returns a fake user with empty identities when email already exists
            // This is intentional to prevent email enumeration attacks
            if (result?.identities.isNullOrEmpty()) {
                return Result.success(SignUpResult.UserAlreadyExists)
            }

            // Check if email confirmation is required by checking the current user
            val currentUser = supabaseClient.auth.currentUserOrNull()
            val userNeedsConfirmation = currentUser?.emailConfirmedAt == null

            if (userNeedsConfirmation) {
                // Sign out the user but set state to awaiting verification
                supabaseClient.auth.signOut()
                _state.value = AuthState.AwaitingEmailVerification(email)
                Result.success(SignUpResult.AwaitingEmailVerification)
            } else {
                Result.success(SignUpResult.Success)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendSignInOtp(email: String): Result<Unit> =
        try {
            supabaseClient.auth.signInWith(OTP) { this.email = email }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun verifyEmailOtp(email: String, token: String, flow: OtpFlow): Result<Unit> =
        try {
            supabaseClient.auth.verifyEmailOtp(
                type = flow.toOtpType(),
                email = email,
                token = token,
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun resendOtp(email: String, flow: OtpFlow): Result<Unit> =
        try {
            when (flow) {
                OtpFlow.SignUp ->
                    supabaseClient.auth.resendEmail(type = OtpType.Email.SIGNUP, email = email)
                OtpFlow.PasswordlessSignIn ->
                    supabaseClient.auth.signInWith(OTP) { this.email = email }
                OtpFlow.EmailChange ->
                    supabaseClient.auth.resendEmail(
                        type = OtpType.Email.EMAIL_CHANGE,
                        email = email,
                    )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun OtpFlow.toOtpType(): OtpType.Email =
        when (this) {
            OtpFlow.SignUp -> OtpType.Email.SIGNUP
            OtpFlow.PasswordlessSignIn -> OtpType.Email.EMAIL
            OtpFlow.EmailChange -> OtpType.Email.EMAIL_CHANGE
        }

    override suspend fun signOut() {
        try {
            supabaseClient.auth.signOut()
        } catch (e: Exception) {
            // Log error but don't throw - sign out should be best effort
            println("Error signing out: ${e.message}")
        }
    }

    override suspend fun updateProfile(displayName: String, avatarUrl: String?): Result<Unit> =
        try {
            supabaseClient.auth.updateUser {
                data = buildJsonObject {
                    put("name", displayName)
                    avatarUrl?.let { put("avatar_url", it) }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.w(throwable = e, tag = TAG) { "Failed to update profile" }
            Result.failure(e)
        }

    override suspend fun deleteAccount(): Result<Unit> =
        try {
            // The client SDK can't delete auth users, so the actual deletion happens in a
            // service-role Edge Function. It reads the caller's JWT and admin-deletes that user.
            supabaseClient.functions.invoke("delete-account")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.w(throwable = e, tag = TAG) { "Failed to delete account" }
            Result.failure(e)
        }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun UserInfo.toChefMateUser(isAnonymous: Boolean): ChefMateUser =
        ChefMateUser(
            userId = id,
            userName =
                userMetadata.stringValue("name")
                    ?: userMetadata.stringValue("username")
                    ?: email?.substringBefore("@")
                    ?: "User",
            userEmail = email ?: "",
            userProfileImageUrl =
                userMetadata.stringValue("avatar_url") ?: userMetadata.stringValue("picture"),
            isAnonymous = isAnonymous,
        )

    /**
     * Reads a string metadata value as its raw content. [JsonElement.toString] would wrap string
     * primitives in literal quotes (e.g. `"Chef"`), which previously leaked into the display name
     * and corrupted the `avatar_url` into an unloadable quoted URL.
     */
    private fun JsonObject?.stringValue(key: String): String? =
        (this?.get(key) as? JsonPrimitive)?.contentOrNull

    /**
     * Reads the `is_anonymous` claim from the JWT payload directly. supabase-kt 3.2.6's UserInfo
     * doesn't deserialize that top-level claim into a property, and `appMetadata.provider` is
     * `null` (not `"anonymous"`) in current Supabase versions — JWT inspection is the only reliable
     * signal.
     */
    @OptIn(ExperimentalEncodingApi::class)
    private fun isAnonymousJwt(accessToken: String): Boolean {
        val payload = accessToken.split(".").getOrNull(1) ?: return false
        return try {
            // JWT uses base64url without padding; pad to a multiple of 4 before decoding.
            val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
            val decoded = Base64.UrlSafe.decode(padded).decodeToString()
            Json.parseToJsonElement(decoded)
                .jsonObject["is_anonymous"]
                ?.jsonPrimitive
                ?.booleanOrNull == true
        } catch (t: Throwable) {
            Logger.w(throwable = t, tag = TAG) { "Failed to decode JWT for is_anonymous check" }
            false
        }
    }

    private companion object {
        const val TAG = "SupabaseAuthenticationRepository"
    }
}
