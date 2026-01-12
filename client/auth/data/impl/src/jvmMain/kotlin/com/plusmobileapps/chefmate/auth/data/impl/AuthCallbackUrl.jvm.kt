package com.plusmobileapps.chefmate.auth.data.impl

/**
 * JVM Desktop uses a custom URL scheme deep link.
 * This is registered via jpackage:
 * - macOS: Info.plist CFBundleURLTypes
 * - Linux: .desktop file with MimeType
 * - Windows: Registry entries via installer
 */
actual val authCallbackUrl: String = "chefmate://auth/callback"
