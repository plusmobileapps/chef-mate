package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import chefmate.client.onboarding.public.generated.resources.Res
import coil3.compose.AsyncImage

// iOS's Skia build has no gif codec at all — org.jetbrains.skia.Codec.makeFromData throws
// "Unsupported format" for gif bytes (confirmed via a failing iosSimulatorArm64Test run), unlike
// the
// desktop Skia build used by AnimatedGif.jvm.kt, which does include one. So iOS falls back to Coil
// (as it did before this animation was added): Coil's default Skia decoder still renders the gif's
// first frame as a static image.
@Composable
internal actual fun AnimatedGif(
    resourcePath: String,
    contentScale: ContentScale,
    modifier: Modifier,
) {
    AsyncImage(
        model = Res.getUri(resourcePath),
        contentDescription = null, // decorative; the step title and body describe the feature
        contentScale = contentScale,
        modifier = modifier,
    )
}
