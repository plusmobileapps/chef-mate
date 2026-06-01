package com.plusmobileapps.chefmate.root

sealed class DeepLink {
    data object None : DeepLink()

    data class RecipeDetail(val recipeId: Long) : DeepLink()

    data object Groceries : DeepLink()

    data object MealPlanner : DeepLink()

    data object AppSettings : DeepLink()

    data object SignIn : DeepLink()

    data object SignUp : DeepLink()

    companion object {
        const val SCHEME: String = "chefmate"

        fun parse(uri: String?): DeepLink {
            if (uri.isNullOrBlank()) return None
            val prefix = "$SCHEME://"
            if (!uri.startsWith(prefix)) return None
            val rest = uri.substring(prefix.length).trimEnd('/')
            val segments = rest.split('/').filter { it.isNotEmpty() }
            val host = segments.firstOrNull() ?: return None
            return when (host) {
                "recipe" -> {
                    val id = segments.getOrNull(1)?.toLongOrNull() ?: return None
                    RecipeDetail(id)
                }
                "groceries" -> Groceries
                "meal-planner" -> MealPlanner
                "settings" -> AppSettings
                "signin" -> SignIn
                "signup" -> SignUp
                else -> None
            }
        }
    }
}
