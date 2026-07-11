package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.SearchEngine

internal fun String.toNavigationUrl(engine: SearchEngine): String {
    val trimmed = trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        !trimmed.contains(" ") && trimmed.contains(".") -> "https://$trimmed"
        else -> "${engine.searchUrl}${trimmed.replace(" ", "+")}"
    }
}
