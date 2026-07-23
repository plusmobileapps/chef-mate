package com.plusmobileapps.chefmate

import coil3.ComponentRegistry

// Coil has no animated-gif decoder for non-Android targets; the built-in Skia decoder renders the
// gif's first frame as a static image.
internal actual fun ComponentRegistry.Builder.addAnimatedGifDecoder() = Unit
