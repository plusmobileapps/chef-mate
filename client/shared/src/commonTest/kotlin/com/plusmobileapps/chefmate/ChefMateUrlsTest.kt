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

    @Test
    fun recipeShareUrlRemoteId_round_trips_a_built_link() {
        assertEquals(
            "remote-uuid",
            ChefMateUrls.recipeShareUrlRemoteId(ChefMateUrls.recipeShareUrl("remote-uuid")),
        )
    }

    @Test
    fun recipeShareUrlRemoteId_ignores_query_and_trailing_path() {
        val base = "https://${ChefMateUrls.WEB_HOST}/recipe/id42"
        assertEquals("id42", ChefMateUrls.recipeShareUrlRemoteId("$base/extra"))
        assertEquals("id42", ChefMateUrls.recipeShareUrlRemoteId("$base?x=1"))
    }

    @Test
    fun recipeShareUrlRemoteId_rejects_the_custom_scheme_link() {
        // The chefmate:// link is keyed by clientId, a separate identifier space — not a share
        // link.
        assertNull(ChefMateUrls.recipeShareUrlRemoteId(ChefMateUrls.recipeLink("client-id")))
    }

    @Test
    fun recipeShareUrlRemoteId_rejects_unrelated_and_blank_urls() {
        assertNull(ChefMateUrls.recipeShareUrlRemoteId("https://example.com/recipe/x"))
        assertNull(ChefMateUrls.recipeShareUrlRemoteId("https://${ChefMateUrls.WEB_HOST}/recipe/"))
        assertNull(
            ChefMateUrls.recipeShareUrlRemoteId("https://${ChefMateUrls.WEB_HOST}/groceries")
        )
    }

    @Test
    fun profileShareUrl_builds_the_at_handle_form() {
        assertEquals(
            "https://${ChefMateUrls.WEB_HOST}/@juliachild",
            ChefMateUrls.profileShareUrl("juliachild"),
        )
    }

    @Test
    fun profileShareUrlHandle_round_trips() {
        val url = ChefMateUrls.profileShareUrl("juliachild")
        assertEquals("juliachild", ChefMateUrls.profileShareUrlHandle(url))
        assertEquals("juliachild", ChefMateUrls.profileShareUrlHandle("$url/"))
        assertEquals("juliachild", ChefMateUrls.profileShareUrlHandle("$url?ref=share"))
        assertEquals("juliachild", ChefMateUrls.profileShareUrlHandle("$url#top"))
    }

    @Test
    fun profileShareUrlHandle_rejects_unrelated_and_blank_urls() {
        assertNull(ChefMateUrls.profileShareUrlHandle("https://example.com/@juliachild"))
        assertNull(ChefMateUrls.profileShareUrlHandle("https://${ChefMateUrls.WEB_HOST}/@"))
        assertNull(
            ChefMateUrls.profileShareUrlHandle("https://${ChefMateUrls.WEB_HOST}/recipe/id42")
        )
    }
}
