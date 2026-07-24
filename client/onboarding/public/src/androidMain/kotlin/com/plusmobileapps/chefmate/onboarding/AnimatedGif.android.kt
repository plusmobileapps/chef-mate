package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import chefmate.client.onboarding.public.generated.resources.Res
import coil3.compose.AsyncImage

// Android animates the gif through Coil: the app registers the framework/coil-gif decoder on the
// singleton ImageLoader (see App.addAnimatedGifDecoder), so AsyncImage plays it.
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
