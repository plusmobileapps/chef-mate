package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable

/**
 * Returns an email launcher function. The returned function accepts an email address and opens the
 * platform's default email client with that address pre-filled in the "to" field via a `mailto:`
 * link. Unlike opening a URL in the in-app browser, this hands off to the OS mail app.
 */
@Composable expect fun rememberEmailLauncher(): (email: String) -> Unit
