package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

/**
 * Draws the animated gif at [resourcePath] — a Compose resource path such as
 * "files/onboarding_share_ios.gif".
 *
 * Android decodes and animates it with Coil (the framework/`coil-gif` decoder registered on the
 * app's ImageLoader). Desktop has no Coil gif decoder, so it decodes frames with
 * `org.jetbrains.skia.Codec` and drives the animation itself. iOS also has no Coil gif decoder, but
 * unlike desktop its Skia build has no gif codec at all (`Codec.makeFromData` throws "Unsupported
 * format" for gif bytes), so it falls back to Coil, which shows a static first frame.
 */
@Composable
internal expect fun AnimatedGif(
    resourcePath: String,
    contentScale: ContentScale,
    modifier: Modifier,
)
