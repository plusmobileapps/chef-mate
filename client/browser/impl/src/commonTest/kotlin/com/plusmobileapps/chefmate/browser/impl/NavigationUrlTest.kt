@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.browser.impl

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NavigationUrlTest {

    @Test
    fun When_input_has_https_scheme_Then_returned_as_is() {
        "https://example.com".toNavigationUrl() shouldBe "https://example.com"
    }

    @Test
    fun When_input_has_http_scheme_Then_returned_as_is() {
        "http://example.com".toNavigationUrl() shouldBe "http://example.com"
    }

    @Test
    fun When_input_is_a_bare_domain_Then_https_is_prepended() {
        "example.com".toNavigationUrl() shouldBe "https://example.com"
    }

    @Test
    fun When_input_is_a_domain_with_path_Then_https_is_prepended() {
        "example.com/recipes".toNavigationUrl() shouldBe "https://example.com/recipes"
    }

    @Test
    fun When_input_contains_spaces_Then_routed_to_google_search() {
        "chicken pasta".toNavigationUrl() shouldBe "https://www.google.com/search?q=chicken+pasta"
    }

    @Test
    fun When_input_has_no_dot_and_no_spaces_Then_routed_to_google_search() {
        "pasta".toNavigationUrl() shouldBe "https://www.google.com/search?q=pasta"
    }

    @Test
    fun When_input_has_leading_and_trailing_whitespace_Then_trimmed_before_classification() {
        "   example.com   ".toNavigationUrl() shouldBe "https://example.com"
    }
}
