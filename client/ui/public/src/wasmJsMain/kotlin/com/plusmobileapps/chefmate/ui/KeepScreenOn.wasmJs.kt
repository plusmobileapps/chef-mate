@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui

import androidx.compose.runtime.Composable

@Composable
actual fun KeepScreenOn() {
    // No-op on web. The Screen Wake Lock API could be wired here later.
}
