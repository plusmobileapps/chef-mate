package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable

/** Keeps the device screen on while this composable is in composition. Cleans up on dispose. */
@Composable expect fun KeepScreenOn()
