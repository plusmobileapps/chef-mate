package com.plusmobileapps.chefmate.ui.components

import androidx.compose.foundation.text.KeyboardOptions

/**
 * Returns [KeyboardOptions] opted into the platform-native text input where one is available.
 *
 * On iOS (Compose Multiplatform 1.11+) this enables the experimental native UIView-backed text
 * input, which gives precise caret movement, native gestures and selection handles, and the
 * familiar system context menu (Autofill, Translate, Search). On every other platform it returns
 * the options unchanged.
 */
expect fun KeyboardOptions.withNativeTextInput(): KeyboardOptions
