package com.plusmobileapps.chefmate.ui

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** True when this screen is the topmost active child in the root navigation stack. */
val LocalIsActiveScreen = compositionLocalOf { true }
