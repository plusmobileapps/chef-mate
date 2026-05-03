package com.plusmobileapps.chefmate.browser.impl

internal fun String.toNavigationUrl(): String {
    val trimmed = trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        !trimmed.contains(" ") && trimmed.contains(".") -> "https://$trimmed"
        else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
    }
}
