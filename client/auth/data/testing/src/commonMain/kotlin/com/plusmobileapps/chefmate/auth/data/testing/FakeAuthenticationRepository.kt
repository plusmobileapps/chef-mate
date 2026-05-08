package com.plusmobileapps.chefmate.auth.data.testing

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.auth.data.OtpFlow
import com.plusmobileapps.chefmate.auth.data.SignUpResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAuthenticationRepository : AuthenticationRepository {
    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state

    var signInResult: Result<Unit> = Result.success(Unit)
    var signUpResult: Result<SignUpResult> = Result.success(SignUpResult.Success)
    var sendPasswordResetResult: Result<Unit> = Result.success(Unit)
    var sendSignInOtpResult: Result<Unit> = Result.success(Unit)
    var verifyEmailOtpResult: Result<Unit> = Result.success(Unit)
    var resendOtpResult: Result<Unit> = Result.success(Unit)

    var lastVerifyOtpFlow: OtpFlow? = null
        private set

    var lastResendOtpFlow: OtpFlow? = null
        private set

    fun setState(state: AuthState) {
        _state.value = state
    }

    fun setAuthenticated(user: ChefMateUser = fakeUser()) {
        _state.value = AuthState.Authenticated(user)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Result<Unit> =
        signInResult

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<SignUpResult> = signUpResult

    override suspend fun signOut() {
        _state.value = AuthState.Unauthenticated
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        sendPasswordResetResult

    override suspend fun sendSignInOtp(email: String): Result<Unit> = sendSignInOtpResult

    override suspend fun verifyEmailOtp(email: String, token: String, flow: OtpFlow): Result<Unit> {
        lastVerifyOtpFlow = flow
        return verifyEmailOtpResult.also { result ->
            if (result.isSuccess) _state.value = AuthState.Authenticated(fakeUser(email))
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
