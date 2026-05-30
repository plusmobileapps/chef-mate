@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.recipe.importer.impl

import java.io.ByteArrayOutputStream
import java.util.zip.Inflater

internal actual fun inflateRaw(data: ByteArray, expectedSize: Int): ByteArray {
    if (data.isEmpty()) return ByteArray(0)
    val inflater = Inflater(true)
    inflater.setInput(data)
    val buffer = ByteArray(if (expectedSize > 0) minOf(expectedSize, 64 * 1024) else 8192)
    val out = ByteArrayOutputStream(maxOf(expectedSize, 64))
    try {
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            if (count == 0 && (inflater.needsInput() || inflater.needsDictionary())) break
            out.write(buffer, 0, count)
        }
    } finally {
        inflater.end()
    }
    return out.toByteArray()
}
