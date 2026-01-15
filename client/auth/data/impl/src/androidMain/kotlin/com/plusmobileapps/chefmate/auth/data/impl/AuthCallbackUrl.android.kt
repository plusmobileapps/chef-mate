package com.plusmobileapps.chefmate.auth.data.impl

/**
 * Android uses a custom URL scheme deep link.
 * This is registered in AndroidManifest.xml via intent-filter.
 */
actual val authCallbackUrl: String = "chefmate://auth/callback"
