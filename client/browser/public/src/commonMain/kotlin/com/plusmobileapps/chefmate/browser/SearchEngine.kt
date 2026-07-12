package com.plusmobileapps.chefmate.browser

/**
 * A web search engine the user can pick as their default for the in-app browser.
 *
 * [id] is the stable value persisted in [BrowserPreferences] (never localize or reorder it).
 * [searchUrl] is a prefix onto which the URL-encoded query is appended (see `toNavigationUrl`).
 */
enum class SearchEngine(val id: String, val displayName: String, val searchUrl: String) {
    GOOGLE("google", "Google", "https://www.google.com/search?q="),
    DUCK_DUCK_GO("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q="),
    BING("bing", "Bing", "https://www.bing.com/search?q="),
    BRAVE("brave", "Brave", "https://search.brave.com/search?q=");

    companion object {
        fun fromId(id: String?): SearchEngine? = entries.firstOrNull { it.id == id }
    }
}
