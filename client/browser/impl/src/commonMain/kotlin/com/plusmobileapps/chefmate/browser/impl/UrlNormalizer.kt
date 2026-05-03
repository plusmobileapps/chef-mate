package com.plusmobileapps.chefmate.browser.impl

internal object UrlNormalizer {
    private val SKIP_HOSTS_WITH_PATH_PREFIX =
        listOf("www.google.com" to "/search", "google.com" to "/search")

    fun normalize(rawUrl: String): String? {
        val trimmed =
            rawUrl.trim().ifEmpty {
                return null
            }
        val withoutFragment = trimmed.substringBefore('#')
        if (withoutFragment.isEmpty()) return null
        if (isDefaultLanding(withoutFragment)) return null
        if (isSearchEnginePage(withoutFragment)) return null
        return withoutFragment
    }

    private fun isDefaultLanding(url: String): Boolean =
        url == "https://www.google.com" || url == "https://www.google.com/"

    private fun isSearchEnginePage(url: String): Boolean {
        val schemeStripped = url.removePrefix("https://").removePrefix("http://")
        val hostEnd = schemeStripped.indexOfAny(charArrayOf('/', '?'))
        val host = if (hostEnd == -1) schemeStripped else schemeStripped.substring(0, hostEnd)
        val path =
            if (hostEnd == -1) "/" else schemeStripped.substring(hostEnd).substringBefore('?')
        return SKIP_HOSTS_WITH_PATH_PREFIX.any { (h, p) -> host == h && path.startsWith(p) }
    }
}
