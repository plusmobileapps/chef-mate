@file:Suppress("ktlint:standard:filename")
@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.plusmobileapps.chefmate.recipe.importer.impl

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDataCompressionAlgorithmZlib
import platform.Foundation.create
import platform.Foundation.decompressedDataUsingAlgorithm
import platform.posix.memcpy

// Foundation's "Zlib" algorithm operates on a raw DEFLATE stream (no zlib/gzip wrapper), which is
// exactly what a zip entry's deflated payload is.
internal actual fun inflateRaw(data: ByteArray, expectedSize: Int): ByteArray {
    if (data.isEmpty()) return ByteArray(0)
    val decompressed =
        data.toNSData().decompressedDataUsingAlgorithm(NSDataCompressionAlgorithmZlib, null)
            ?: error("Failed to inflate zip entry")
    return decompressed.toByteArray()
}

private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    if (size == 0) return ByteArray(0)
    return ByteArray(size).also { array ->
        array.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, length) }
    }
}
