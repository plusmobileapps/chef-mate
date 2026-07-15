package com.plusmobileapps.chefmate

/**
 * Canonical Chef Mate deep-link scheme/host and the builders for share links. Lives in
 * `client:shared` so both `root:public` (which parses links) and feature modules (which build them)
 * can depend on it without a cycle — `recipe:core:public` can't depend on `root:public` because
 * `root:public` already depends on it.
 */
object ChefMateUrls {
    const val SCHEME: String = "chefmate"

    /**
     * Host of the Universal Link / App Link web domain, e.g. https://chefmate.plusmobileapps.com.
     */
    const val WEB_HOST: String = "chefmate.plusmobileapps.com"

    /**
     * The shareable web link to a recipe, keyed by its global [remoteId] (a Supabase UUID). Opens
     * the recipe in the app via App Links / Universal Links. The remote id — not the local recipe
     * id — is used so the link resolves to the same recipe for every user.
     */
    fun recipeShareUrl(remoteId: String): String = "https://$WEB_HOST/recipe/$remoteId"

    /**
     * An in-app recipe-to-recipe link embedded in ingredient/direction markdown, keyed by the
     * target recipe's device-generated [clientId]. Unlike [recipeShareUrl] (a public https link
     * keyed by remoteId), this is owner-scoped and resolved locally via
     * `RecipeRepository.getRecipeByClientId`, so it works offline and survives sync to any device.
     */
    fun recipeLink(clientId: String): String = "$SCHEME://recipe/$clientId"

    /**
     * Extracts the target clientId from a [recipeLink], or null if [url] is not one. Only the
     * custom `chefmate://recipe/<clientId>` scheme form matches; the public https share link (keyed
     * by remoteId) is intentionally excluded so the two identifier spaces stay separate.
     */
    fun recipeLinkClientId(url: String): String? {
        val prefix = "$SCHEME://recipe/"
        if (!url.startsWith(prefix)) return null
        val clientId = url.substring(prefix.length).substringBefore('/').substringBefore('?')
        return clientId.ifBlank { null }
    }
}
