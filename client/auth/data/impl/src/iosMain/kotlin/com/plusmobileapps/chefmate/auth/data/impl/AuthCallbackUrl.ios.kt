package com.plusmobileapps.chefmate.auth.data.impl

/**
 * iOS uses a custom URL scheme deep link.
 * This is registered in Info.plist via CFBundleURLTypes.
 */
actual val authCallbackUrl: String = "chefmate://auth/callback"
