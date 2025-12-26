package com.plusmobileapps.chefmate.auth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun AuthenticationScreen(
    bloc: AuthenticationBloc,
    modifier: Modifier = Modifier,
) {
    // TODO: Implement full authentication UI
    // - Sign in form with email and password fields
    // - Sign up form
    // - Toggle between sign in and sign up
    // - Handle authentication state and errors
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Authentication Screen - TODO: Implement UI")
    }
}
