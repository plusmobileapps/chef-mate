package com.plusmobileapps.chefmate.root

sealed class DeepLink {
    data object None : DeepLink()

    data class RecipeDetail(val recipeId: Long) : DeepLink()

    data object Groceries : DeepLink()

    data object MealPlanner : DeepLink()

    data object AppSettings : DeepLink()

    data object Notifications : DeepLink()

    data object SignIn : DeepLink()

    data object SignUp : DeepLink()

    companion object {
        const val SCHEME: String = "chefmate"

        /**
         * Host of the Universal Link / App Link web domain, e.g.
         * https://chefmate.plusmobileapps.com.
         */
        const val WEB_HOST: String = "chefmate.plusmobileapps.com"

        fun parse(uri: String?): DeepLink {
            if (uri.isNullOrBlank()) return None
            val segments = pathSegments(uri) ?: return None
            val host = segments.firstOrNull() ?: return None
            return when (host) {
                "recipe" -> {
                    val id = segments.getOrNull(1)?.toLongOrNull() ?: return None
                    RecipeDetail(id)
                }
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
