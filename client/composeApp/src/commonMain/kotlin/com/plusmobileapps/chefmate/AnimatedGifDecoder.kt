package com.plusmobileapps.chefmate

import coil3.ComponentRegistry

/**
 * Registers the platform's animated-gif [coil3.decode.Decoder] on the Coil image loader, if it has
 * one.
 *
 * Animated gif decoding is Android-only in Coil (the `coil-gif` artifact ships no non-Android
 * variant). On the other targets this is a no-op: Coil's built-in Skia decoder still renders the
 * gif's first frame as a static image, so a gif preview shows something everywhere but only
 * animates on Android.
 */
internal expect fun ComponentRegistry.Builder.addAnimatedGifDecoder()
