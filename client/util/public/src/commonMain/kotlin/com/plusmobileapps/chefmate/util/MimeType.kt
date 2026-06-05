package com.plusmobileapps.chefmate.util

/**
 * Maps a picked image file extension (as returned by
 * [com.plusmobileapps.chefmate.util.PickedImage.fileExtension]) to the MIME type the Gemini vision
 * API expects on an `inlineData` part. Unknown extensions fall back to `image/jpeg`, which Gemini
 * accepts for most photo encodings.
 */
fun mimeTypeForImageExtension(fileExtension: String): String =
    when (fileExtension.trim().lowercase().removePrefix(".")) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        "gif" -> "image/gif"
        else -> "image/jpeg"
    }
