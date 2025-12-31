package com.plusmobileapps.chefmate.auth.data

sealed class AuthState {
    data object Unauthenticated : AuthState()

    data class Authenticated(
        val user: ChefMateUser,
    ) : AuthState()
}
