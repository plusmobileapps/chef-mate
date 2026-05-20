package com.plusmobileapps.chefmate.util

import androidx.compose.ui.graphics.ImageBitmap

/** Decodes encoded image bytes (JPEG/PNG/etc.) into a Compose [ImageBitmap]. */
expect fun decodeImageBitmap(bytes: ByteArray): ImageBitmap

/**
 * Crops the encoded image to a square sub-region and re-encodes as JPEG. The output is also
 * downscaled to [maxOutputDim] on each side when the source square is larger.
 */
expect fun cropImageToSquare(
    bytes: ByteArray,
    srcX: Int,
    srcY: Int,
    srcSize: Int,
    maxOutputDim: Int = 1024,
): ByteArray
