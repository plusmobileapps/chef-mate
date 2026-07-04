@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.text.KeyboardOptions

// Desktop has no native text input to opt into, so this is a no-op.
actual fun KeyboardOptions.withNativeTextInput(): KeyboardOptions = this
