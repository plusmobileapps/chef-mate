@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.PlatformImeOptions

@OptIn(ExperimentalComposeUiApi::class)
actual fun KeyboardOptions.withNativeTextInput(): KeyboardOptions =
    copy(
        platformImeOptions =
            PlatformImeOptions {
                usingNativeTextInput(true)
            }
    )
