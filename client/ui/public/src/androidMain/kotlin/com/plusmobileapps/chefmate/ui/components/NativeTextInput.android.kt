@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.text.KeyboardOptions

// Android already uses the platform-native text input, so this is a no-op.
actual fun KeyboardOptions.withNativeTextInput(): KeyboardOptions = this
