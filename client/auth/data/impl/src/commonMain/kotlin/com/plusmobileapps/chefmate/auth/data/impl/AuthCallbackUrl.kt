package com.plusmobileapps.chefmate.auth.data.impl

/**
 * Platform-specific auth callback URL for Supabase email verification deep links.
 *
 * - Android/iOS: Uses custom URL scheme (chefmate://auth/callback)
 * - JVM Desktop: Uses custom URL scheme (chefmate://auth/callback) registered via jpackage
 */
expect val authCallbackUrl: String
