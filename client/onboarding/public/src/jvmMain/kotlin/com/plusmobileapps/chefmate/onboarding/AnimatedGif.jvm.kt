package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import chefmate.client.onboarding.public.generated.resources.Res
import kotlinx.coroutines.delay
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Data
import org.jetbrains.skia.Image

// Desktop has no Coil gif decoder, so decode frames with Skia's Codec and swap them on a timer.
// Frames are read sequentially into one reused bitmap so each frame composes correctly on top of
// the
// previous one; a fresh ImageBitmap snapshot per frame is what triggers recomposition. Kept
// byte-for-
// byte identical to the iOS AnimatedGif.ios.kt (both skiko targets), mirroring the repo's
// ImageProcessingUtil pattern of duplicating Skia code across iosMain/jvmMain.
@Composable
internal actual fun AnimatedGif(
    resourcePath: String,
    contentScale: ContentScale,
    modifier: Modifier,
) {
    var frame by remember(resourcePath) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedGifFrames(resourcePath) { frame = it }

    val current = frame
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = null, // decorative; the step title and body describe the feature
            contentScale = contentScale,
            modifier = modifier,
        )
    } else {
        // Reserve the framed space (same modifier chain) while the first frame decodes.
        Box(modifier)
    }
}

@Composable
private fun LaunchedGifFrames(resourcePath: String, onFrame: (ImageBitmap) -> Unit) {
    LaunchedEffect(resourcePath) {
        val data = Data.makeFromBytes(Res.readBytes(resourcePath))
        val codec = Codec.makeFromData(data)
        val bitmap = Bitmap().apply { allocPixels(codec.imageInfo) }
        try {
            val frameCount = codec.frameCount
            var index = 0
            while (true) {
                codec.readPixels(bitmap, index)
                onFrame(Image.makeFromBitmap(bitmap).toComposeImageBitmap())
                if (frameCount <= 1) break
                val durationMs =
                    codec.framesInfo.getOrNull(index)?.duration?.takeIf { it > 0 }
                        ?: DEFAULT_FRAME_MS
                delay(durationMs.toLong())
                index = (index + 1) % frameCount
            }
        } finally {
            bitmap.close()
            codec.close()
            data.close()
        }
    }
}

private const val DEFAULT_FRAME_MS = 100
