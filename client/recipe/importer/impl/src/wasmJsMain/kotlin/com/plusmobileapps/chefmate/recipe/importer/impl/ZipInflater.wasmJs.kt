@file:Suppress("ktlint:standard:filename")

package com.plusmobileapps.chefmate.recipe.importer.impl

// Stubbed for the web spike — no java.util.zip on wasmJs. A real implementation would use a
// pure-Kotlin DEFLATE decoder or a JS pako binding. Recipe-archive import is unavailable on web
// until then.
internal actual fun inflateRaw(data: ByteArray, expectedSize: Int): ByteArray =
    throw NotImplementedError("Zip inflate is not supported on web (wasmJs) yet.")
