@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import com.plusmobileapps.chefmate.browser.SearchEngine
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NavigationUrlTest {

    @Test
    fun When_input_has_https_scheme_Then_returned_as_is() {
        "https://example.com".toNavigationUrl(SearchEngine.GOOGLE) shouldBe "https://example.com"
    }

    @Test
    fun When_input_has_http_scheme_Then_returned_as_is() {
        "http://example.com".toNavigationUrl(SearchEngine.GOOGLE) shouldBe "http://example.com"
    }

    @Test
    fun When_input_is_a_bare_domain_Then_https_is_prepended() {
        "example.com".toNavigationUrl(SearchEngine.GOOGLE) shouldBe "https://example.com"
    }

    @Test
    fun When_input_is_a_domain_with_path_Then_https_is_prepended() {
        "example.com/recipes".toNavigationUrl(SearchEngine.GOOGLE) shouldBe
            "https://example.com/recipes"
    }

    @Test
    fun When_input_contains_spaces_Then_routed_to_search_with_plus_encoding() {
        "chicken pasta".toNavigationUrl(SearchEngine.GOOGLE) shouldBe
            "https://www.google.com/search?q=chicken+pasta"
    }

    @Test
    fun When_input_has_no_dot_and_no_spaces_Then_routed_to_search() {
        "pasta".toNavigationUrl(SearchEngine.GOOGLE) shouldBe
            "https://www.google.com/search?q=pasta"
    }

    @Test
    fun When_input_has_leading_and_trailing_whitespace_Then_trimmed_before_classification() {
        "   example.com   ".toNavigationUrl(SearchEngine.GOOGLE) shouldBe "https://example.com"
    }

    @Test
    fun When_engine_is_duckduckgo_Then_query_routed_to_duckduckgo() {
        "chicken pasta".toNavigationUrl(SearchEngine.DUCK_DUCK_GO) shouldBe
            "https://duckduckgo.com/?q=chicken+pasta"
    }

    @Test
    fun When_engine_is_bing_Then_query_routed_to_bing() {
        "pasta".toNavigationUrl(SearchEngine.BING) shouldBe "https://www.bing.com/search?q=pasta"
    }

    @Test
    fun When_engine_is_brave_Then_query_routed_to_brave() {
        "pasta".toNavigationUrl(SearchEngine.BRAVE) shouldBe
            "https://search.brave.com/search?q=pasta"
    }

    @Test
    fun When_input_is_a_url_Then_engine_is_ignored() {
        "https://example.com".toNavigationUrl(SearchEngine.BING) shouldBe "https://example.com"
    }
}
