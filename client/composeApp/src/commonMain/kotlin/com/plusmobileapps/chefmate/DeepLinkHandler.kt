package com.plusmobileapps.chefmate

import io.github.aakira.napier.Napier

/**
 * Singleton for handling deep link URIs across platforms.
 *
 * This handler caches URIs that arrive before a listener is set up,
 * then delivers them once the listener is registered.
 *
 * Usage:
 * 1. Platform-specific code calls [onNewUri] when a deep link is received
 * 2. The app sets a [listener] to handle incoming URIs
 * 3. If a URI arrives before the listener is set, it's cached and delivered later
 */
object DeepLinkHandler {
    private var cached: String? = null

    /**
     * Listener for incoming deep link URIs.
     * When set, any cached URI is immediately delivered.
     */
    var listener: ((uri: String) -> Unit)? = null
        set(value) {
            field = value
            if (value != null) {
                cached?.let { uri ->
                    Napier.d("DeepLinkHandler: Delivering cached URI: $uri")
                    value.invoke(uri)
                }
                cached = null
            }
        }

    /**
     * Called by platform-specific code when a new deep link URI is received.
     *
     * @param uri The full deep link URI (e.g., "chefmate://auth/callback#access_token=...")
     */
    fun onNewUri(uri: String) {
        Napier.d("DeepLinkHandler: Received URI: $uri")
        cached = uri
        listener?.let {
            it.invoke(uri)
            cached = null
        }
    }

    /**
     * Clears any cached URI without delivering it.
     */
    fun clearCache() {
        cached = null
    }
}
