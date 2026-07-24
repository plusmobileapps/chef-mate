package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Draws the animated gif at [resourcePath] — a Compose resource path such as
 * "files/onboarding_share_ios.gif".
 *
 * Android decodes and animates it with Coil (the framework/`coil-gif` decoder registered on the
 * app's ImageLoader). iOS and desktop have no Coil gif decoder, so they decode frames with
 * `org.jetbrains.skia.Codec` and drive the animation themselves — otherwise Coil's Skia fallback
 * would only ever show the first frame.
 */
@Composable
internal expect fun AnimatedGif(
    resourcePath: String,
    contentScale: ContentScale,
    modifier: Modifier,
)
