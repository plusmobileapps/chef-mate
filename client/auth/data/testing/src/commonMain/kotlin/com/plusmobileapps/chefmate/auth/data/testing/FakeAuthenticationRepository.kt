package com.plusmobileapps.chefmate.auth.data.testing

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthenticationRepository : AuthenticationRepository {
    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state

    private val _authenticatedSessions =
        MutableSharedFlow<ChefMateUser>(
            replay = 1,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    override val authenticatedSessions: SharedFlow<ChefMateUser> = _authenticatedSessions

    var signInResult: Result<Unit> = Result.success(Unit)
    var signUpResult: Result<SignUpResult> = Result.success(SignUpResult.Success)
    var sendPasswordResetResult: Result<Unit> = Result.success(Unit)
    var sendSignInOtpResult: Result<Unit> = Result.success(Unit)
    var verifyEmailOtpResult: Result<Unit> = Result.success(Unit)
    var resendOtpResult: Result<Unit> = Result.success(Unit)
    var ensureSessionResult: Result<Unit> = Result.success(Unit)
    var updateProfileResult: Result<Unit> = Result.success(Unit)
    var deleteAccountResult: Result<Unit> = Result.success(Unit)

    var lastUpdatedDisplayName: String? = null
        private set

    var lastUpdatedAvatarUrl: String? = null
        private set

    var deleteAccountCallCount: Int = 0
        private set

    var ensureSessionCallCount: Int = 0
        private set

    var lastVerifyOtpFlow: OtpFlow? = null
        private set

    var lastResendOtpFlow: OtpFlow? = null
        private set

    var refreshSessionResult: Boolean = true

    var refreshSessionCallCount: Int = 0
        private set

    fun setState(state: AuthState) {
        emitState(state)
    }

    fun setAuthenticated(user: ChefMateUser = fakeUser()) {
        emitState(AuthState.Authenticated(user))
    }

    fun setAnonymous(userId: String = "anon-test-id") {
        emitState(
            AuthState.Authenticated(
                ChefMateUser(
                    userId = userId,
                    userName = "Guest",
                    userEmail = "",
                    userProfileImageUrl = null,
                    isAnonymous = true,
                )
            )
        )
    }

    /**
     * Emits a fresh session for the currently-signed-in user without changing [state] — what a
     * silent token refresh looks like to collectors. `StateFlow` swallows the identical
     * `Authenticated` value, so this is the only way sync triggers hear about it.
     */
    fun emitSessionRefresh() {
        val user = (_state.value as? AuthState.Authenticated)?.user ?: return
        _authenticatedSessions.tryEmit(user)
    }

    private fun emitState(state: AuthState) {
        _state.value = state
        when (state) {
            is AuthState.Authenticated -> _authenticatedSessions.tryEmit(state.user)
            else -> _authenticatedSessions.resetReplayCache()
        }
    }

    override suspend fun ensureSession(): Result<Unit> {
        ensureSessionCallCount += 1
        return ensureSessionResult.also { result ->
            if (result.isSuccess && _state.value !is AuthState.Authenticated) setAnonymous()
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> =
        signInResult

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<SignUpResult> = signUpResult

    override suspend fun signOut() {
        emitState(AuthState.Unauthenticated)
    }

    override suspend fun refreshSessionIfNeeded(): Boolean {
        refreshSessionCallCount += 1
        return refreshSessionResult
    }

    override suspend fun updateProfile(displayName: String, avatarUrl: String?): Result<Unit> {
        lastUpdatedDisplayName = displayName
        lastUpdatedAvatarUrl = avatarUrl
        return updateProfileResult.also { result ->
            if (result.isSuccess) {
                (_state.value as? AuthState.Authenticated)?.let { authenticated ->
                    emitState(
                        AuthState.Authenticated(
                            authenticated.user.copy(
                                userName = displayName,
                                userProfileImageUrl =
                                    avatarUrl ?: authenticated.user.userProfileImageUrl,
                            )
                        )
                    )
                }
            }
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        deleteAccountCallCount += 1
        return deleteAccountResult
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        sendPasswordResetResult

    override suspend fun sendSignInOtp(email: String): Result<Unit> = sendSignInOtpResult

    override suspend fun verifyEmailOtp(email: String, token: String, flow: OtpFlow): Result<Unit> {
        lastVerifyOtpFlow = flow
        return verifyEmailOtpResult.also { result ->
            if (result.isSuccess) emitState(AuthState.Authenticated(fakeUser(email)))
        }
    }

    override suspend fun resendOtp(email: String, flow: OtpFlow): Result<Unit> {
        lastResendOtpFlow = flow
        return resendOtpResult
    }

    companion object {
        fun fakeUser(email: String = "test@example.com") =
            ChefMateUser(
                userId = "test-id",
                userName = "Test User",
                userEmail = email,
                userProfileImageUrl = null,
            )
    }
}
