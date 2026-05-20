@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlin.math.min
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap =
    Image.makeFromEncoded(bytes).toComposeImageBitmap()

actual fun cropImageToSquare(
    bytes: ByteArray,
    srcX: Int,
    srcY: Int,
    srcSize: Int,
    maxOutputDim: Int,
): ByteArray {
    val image = Image.makeFromEncoded(bytes)
    val outputSize = min(srcSize, maxOutputDim)
    val surface = Surface.makeRasterN32Premul(outputSize, outputSize)
    val canvas = surface.canvas
    canvas.drawImageRect(
        image,
        Rect.makeXYWH(srcX.toFloat(), srcY.toFloat(), srcSize.toFloat(), srcSize.toFloat()),
        Rect.makeXYWH(0f, 0f, outputSize.toFloat(), outputSize.toFloat()),
    )
    val cropped = surface.makeImageSnapshot()
    val data = cropped.encodeToData(EncodedImageFormat.JPEG, 90) ?: error("JPEG encode failed")
    return data.bytes
}
