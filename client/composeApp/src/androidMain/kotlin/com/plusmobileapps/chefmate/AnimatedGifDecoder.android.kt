package com.plusmobileapps.chefmate

import android.os.Build
import coil3.ComponentRegistry
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder

internal actual fun ComponentRegistry.Builder.addAnimatedGifDecoder() {
    // AnimatedImageDecoder is backed by the framework ImageDecoder (API 28+); older devices fall
    // back to the software GifDecoder.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        add(AnimatedImageDecoder.Factory())
    } else {
        add(GifDecoder.Factory())
    }
}
