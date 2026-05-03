@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UrlNormalizerTest {

    @Test
    fun strips_fragment() {
        UrlNormalizer.normalize("https://example.com/page#section") shouldBe
            "https://example.com/page"
    }

    @Test
    fun preserves_query_params() {
        UrlNormalizer.normalize("https://example.com/page?id=1") shouldBe
            "https://example.com/page?id=1"
    }

    @Test
    fun returns_null_for_default_landing_url() {
        UrlNormalizer.normalize("https://www.google.com") shouldBe null
        UrlNormalizer.normalize("https://www.google.com/") shouldBe null
    }

    @Test
    fun returns_null_for_google_search_pages() {
        UrlNormalizer.normalize("https://www.google.com/search?q=lasagna") shouldBe null
        UrlNormalizer.normalize("https://google.com/search?q=lasagna") shouldBe null
    }

    @Test
    fun preserves_non_search_google_pages() {
        UrlNormalizer.normalize("https://www.google.com/maps") shouldBe
            "https://www.google.com/maps"
    }

    @Test
    fun returns_null_for_blank_input() {
        UrlNormalizer.normalize("") shouldBe null
        UrlNormalizer.normalize("   ") shouldBe null
    }

    @Test
    fun returns_null_when_only_fragment() {
        UrlNormalizer.normalize("#fragment") shouldBe null
    }

    @Test
    fun trims_whitespace() {
        UrlNormalizer.normalize("  https://example.com  ") shouldBe "https://example.com"
    }
}
