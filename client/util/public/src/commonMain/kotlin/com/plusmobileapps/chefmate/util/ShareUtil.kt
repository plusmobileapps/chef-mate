package com.plusmobileapps.chefmate.util

import androidx.compose.runtime.Composable

/**
 * Returns a share launcher function. The returned function accepts text to share and returns `true`
 * if the content was copied to the clipboard (desktop) rather than shared via a system share sheet.
 */
@Composable expect fun rememberShareLauncher(): (text: String) -> Boolean
