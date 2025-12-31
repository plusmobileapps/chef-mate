package com.plusmobileapps.chefmate.auth.data.impl

import com.plusmobileapps.chefmate.auth.data.AuthState
import com.plusmobileapps.chefmate.auth.data.AuthenticationRepository
import com.plusmobileapps.chefmate.auth.data.ChefMateUser
import com.plusmobileapps.chefmate.di.Main
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesBinding
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn
import kotlin.coroutines.CoroutineContext

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, boundType = AuthenticationRepository::class)
class SupabaseAuthenticationRepository(
    private val supabaseClient: SupabaseClient,
    @Main private val mainContext: CoroutineContext,
) : AuthenticationRepository {
    private val scope = CoroutineScope(mainContext)

    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Listen to auth state changes
        scope.launch {
            supabaseClient.auth.sessionStatus.collect { sessionStatus ->
                when (sessionStatus) {
                    is SessionStatus.Authenticated -> {
                        val user = sessionStatus.session.user
                        user?.let {
                            _state.value = AuthState.Authenticated(it.toChefMateUser())
                        }
                    }
                    is SessionStatus.NotAuthenticated -> {
                        _state.value = AuthState.Unauthenticated
                    }
                    is SessionStatus.Initializing,
                    is SessionStatus.RefreshFailure,
                    -> {
                        // Keep current state during initialization or refresh failures
                    }
                }
            }
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<ChefMateUser> =
        try {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabaseClient.auth.currentUserOrNull()
            if (user != null) {
                Result.success(user.toChefMateUser())
            } else {
                Result.failure(Exception("Sign in succeeded but no user found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
    ): Result<ChefMateUser> =
        try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val user = supabaseClient.auth.currentUserOrNull()
            if (user != null) {
                Result.success(user.toChefMateUser())
            } else {
                Result.failure(Exception("Sign up succeeded but no user found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun signOut() {
        try {
            supabaseClient.auth.signOut()
        } catch (e: Exception) {
            // Log error but don't throw - sign out should be best effort
            println("Error signing out: ${e.message}")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> =
        try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun UserInfo.toChefMateUser(): ChefMateUser =
        ChefMateUser(
            userId = id,
            userName =
                userMetadata?.get("name")?.toString()
                    ?: userMetadata?.get("username")?.toString()
                    ?: email?.substringBefore("@")
                    ?: "User",
            userEmail = email ?: "",
            userProfileImageUrl =
                userMetadata?.get("avatar_url")?.toString()
                    ?: userMetadata?.get("picture")?.toString(),
        )
}
