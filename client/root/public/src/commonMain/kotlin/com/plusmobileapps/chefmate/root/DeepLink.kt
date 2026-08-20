package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.ChefMateUrls

sealed class DeepLink {
    data object None : DeepLink()

    data class RecipeDetail(val recipeId: Long) : DeepLink()

    /**
     * A shared recipe link keyed by the recipe's global [remoteId] (a Supabase UUID). Distinct from
     * [RecipeDetail], whose id is a device-local `Long` only meaningful to the current user; this
     * is the cross-user share form (`https://chefmate.plusmobileapps.com/recipe/<uuid>`).
     */
    data class PublicRecipe(val remoteId: String) : DeepLink()

    /**
     * A public profile link, `https://chefmate.plusmobileapps.com/@handle` (or
     * `chefmate://profile/<handle>`).
     */
    data class Profile(val handle: String) : DeepLink()

    data object Groceries : DeepLink()

    data object MealPlanner : DeepLink()

    data object AppSettings : DeepLink()

    data object Notifications : DeepLink()

    data object SignIn : DeepLink()

    data object SignUp : DeepLink()

    companion object {
        const val SCHEME: String = ChefMateUrls.SCHEME

        /**
         * Host of the Universal Link / App Link web domain, e.g.
         * https://chefmate.plusmobileapps.com.
         */
        const val WEB_HOST: String = ChefMateUrls.WEB_HOST

        fun parse(uri: String?): DeepLink {
            if (uri.isNullOrBlank()) return None
            val segments = pathSegments(uri) ?: return None
            val host = segments.firstOrNull() ?: return None
            // The web form of a profile link is `/@handle`, so the handle IS the first segment.
            // Checked before the route table so an `@`-prefixed segment can never fall through to
            // a route name.
            if (host.startsWith("@")) {
                return host.drop(1).ifBlank { null }?.let { Profile(it) } ?: None
            }
            return when (host) {
                "recipe" -> {
                    val segment = segments.getOrNull(1) ?: return None
                    // A numeric id is the device-local RecipeDetail (dev/internal navigation); a
                    // non-numeric segment is a global remote UUID from a cross-user share link.
                    segment.toLongOrNull()?.let { RecipeDetail(it) } ?: PublicRecipe(segment)
                }
                // Profiles live in their own `@`-prefixed namespace, so a handle can never be
                // mistaken for a route like "settings" (the server also refuses those as handles).
                "profile" -> segments.getOrNull(1)?.ifBlank { null }?.let { Profile(it) } ?: None
                "groceries" -> Groceries
                "meal-planner" -> MealPlanner
                "settings" -> AppSettings
                "notifications" -> Notifications
                "signin" -> SignIn
                "signup" -> SignUp
                else -> None
            }
        }

        /**
         * The path segments of a supported deep link, or null if [uri] isn't one. Accepts both the
         * custom scheme (`chefmate://<host>/...`, used for dev + in-app share) and the
         * Universal/App Link web form (`https://chefmate.plusmobileapps.com/<path>`, used by the
         * invite emails). For the web form the leading domain is dropped so both map to the same
         * `host`/segment routing.
         */
        private fun pathSegments(uri: String): List<String>? {
            val schemePrefix = "$SCHEME://"
            val rest =
                when {
                    uri.startsWith(schemePrefix) -> uri.substring(schemePrefix.length)
                    else -> {
                        val afterScheme =
                            uri.substringAfter("://", missingDelimiterValue = "").ifEmpty {
                                return null
                            }
                        // Only our own web host maps to an in-app destination. Compare the whole
                        // host authority (up to the path) so lookalikes like
                        // "chefmate.plusmobileapps.com.evil.com" don't match by prefix.
                        val host = afterScheme.substringBefore('/').substringBefore('?')
                        if (!host.equals(WEB_HOST, ignoreCase = true)) return null
                        afterScheme.substring(host.length)
                    }
                }
            // Drop any query/fragment before splitting into path segments.
            val path = rest.substringBefore('?').substringBefore('#').trim('/')
            return path.split('/').filter { it.isNotEmpty() }
        }
    }
}
