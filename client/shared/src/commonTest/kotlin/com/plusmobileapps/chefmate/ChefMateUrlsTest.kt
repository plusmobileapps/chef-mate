@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ChefMateUrlsTest {

    @Test
    fun recipeLink_builds_the_custom_scheme_form() {
        assertEquals("chefmate://recipe/abc-123", ChefMateUrls.recipeLink("abc-123"))
    }

    @Test
    fun recipeLinkClientId_round_trips_a_built_link() {
        val clientId = "9f2c-uuid"
        assertEquals(clientId, ChefMateUrls.recipeLinkClientId(ChefMateUrls.recipeLink(clientId)))
    }

    @Test
    fun recipeLinkClientId_ignores_query_and_trailing_path() {
        assertEquals("id42", ChefMateUrls.recipeLinkClientId("chefmate://recipe/id42/extra"))
        assertEquals("id42", ChefMateUrls.recipeLinkClientId("chefmate://recipe/id42?x=1"))
    }

    @Test
    fun recipeLinkClientId_rejects_the_public_https_share_link() {
        // The https share link is keyed by remoteId, a separate identifier space — not a client
        // link.
        assertNull(ChefMateUrls.recipeLinkClientId(ChefMateUrls.recipeShareUrl("remote-uuid")))
    }

    @Test
    fun recipeLinkClientId_rejects_unrelated_and_blank_urls() {
        assertNull(ChefMateUrls.recipeLinkClientId("https://example.com/recipe/x"))
        assertNull(ChefMateUrls.recipeLinkClientId("chefmate://recipe/"))
        assertNull(ChefMateUrls.recipeLinkClientId("chefmate://groceries"))
    }
}
