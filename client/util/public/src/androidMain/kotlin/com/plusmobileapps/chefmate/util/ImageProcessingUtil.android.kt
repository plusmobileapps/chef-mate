@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import kotlin.math.min

actual fun decodeImageBitmap(bytes: ByteArray): ImageBitmap =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()

actual fun cropImageToSquare(
    bytes: ByteArray,
    srcX: Int,
    srcY: Int,
    srcSize: Int,
    maxOutputDim: Int,
): ByteArray {
    val src = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val cropped = Bitmap.createBitmap(src, srcX, srcY, srcSize, srcSize)
    val finalSize = min(srcSize, maxOutputDim)
    val output =
        if (finalSize == srcSize) cropped
        else Bitmap.createScaledBitmap(cropped, finalSize, finalSize, true)
    val baos = ByteArrayOutputStream()
    output.compress(Bitmap.CompressFormat.JPEG, 90, baos)
    return baos.toByteArray()
}
