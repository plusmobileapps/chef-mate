@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.root

import com.plusmobileapps.chefmate.ChefMateUrls
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DeepLinkTest {
    @Test
    fun parse_returns_none_for_null_or_blank() {
        DeepLink.parse(null) shouldBe DeepLink.None
        DeepLink.parse("") shouldBe DeepLink.None
        DeepLink.parse("   ") shouldBe DeepLink.None
    }

    @Test
    fun parse_returns_none_for_unknown_scheme() {
        DeepLink.parse("https://example.com/recipe/1") shouldBe DeepLink.None
    }

    @Test
    fun parse_returns_none_for_unknown_host() {
        DeepLink.parse("chefmate://unknown") shouldBe DeepLink.None
    }

    @Test
    fun parse_recipe_detail_with_id() {
        DeepLink.parse("chefmate://recipe/42") shouldBe DeepLink.RecipeDetail(42L)
    }

    @Test
    fun parse_recipe_detail_returns_none_when_id_segment_is_missing() {
        DeepLink.parse("chefmate://recipe") shouldBe DeepLink.None
        DeepLink.parse("chefmate://recipe/") shouldBe DeepLink.None
    }

    @Test
    fun parse_non_numeric_recipe_segment_is_a_public_share_link() {
        // A non-numeric id is a global remote UUID from a cross-user share link, not a local id.
        DeepLink.parse("chefmate://recipe/abc") shouldBe DeepLink.PublicRecipe("abc")
        val uuid = "3f2504e0-4f89-41d3-9a0c-0305e82c3301"
        DeepLink.parse("https://chefmate.plusmobileapps.com/recipe/$uuid") shouldBe
            DeepLink.PublicRecipe(uuid)
    }

    @Test
    fun recipe_share_url_round_trips_through_parse() {
        val uuid = "3f2504e0-4f89-41d3-9a0c-0305e82c3301"
        DeepLink.parse(ChefMateUrls.recipeShareUrl(uuid)) shouldBe DeepLink.PublicRecipe(uuid)
    }

    @Test
    fun parse_known_hosts() {
        DeepLink.parse("chefmate://groceries") shouldBe DeepLink.Groceries
        DeepLink.parse("chefmate://meal-planner") shouldBe DeepLink.MealPlanner
        DeepLink.parse("chefmate://settings") shouldBe DeepLink.AppSettings
        DeepLink.parse("chefmate://notifications") shouldBe DeepLink.Notifications
        DeepLink.parse("chefmate://signin") shouldBe DeepLink.SignIn
        DeepLink.parse("chefmate://signup") shouldBe DeepLink.SignUp
    }

    @Test
    fun parse_tolerates_trailing_slash() {
        DeepLink.parse("chefmate://groceries/") shouldBe DeepLink.Groceries
        DeepLink.parse("chefmate://recipe/42/") shouldBe DeepLink.RecipeDetail(42L)
    }

    @Test
    fun parse_https_app_links_for_our_web_host() {
        DeepLink.parse("https://chefmate.plusmobileapps.com/notifications") shouldBe
            DeepLink.Notifications
        DeepLink.parse("https://chefmate.plusmobileapps.com/notifications/") shouldBe
            DeepLink.Notifications
        DeepLink.parse("https://chefmate.plusmobileapps.com/recipe/42") shouldBe
            DeepLink.RecipeDetail(42L)
        DeepLink.parse("https://chefmate.plusmobileapps.com/groceries") shouldBe DeepLink.Groceries
    }

    @Test
    fun parse_ignores_query_and_fragment() {
        DeepLink.parse("https://chefmate.plusmobileapps.com/notifications?ref=email") shouldBe
            DeepLink.Notifications
        DeepLink.parse("chefmate://notifications#top") shouldBe DeepLink.Notifications
    }

    @Test
    fun parse_returns_none_for_other_https_hosts() {
        DeepLink.parse("https://example.com/notifications") shouldBe DeepLink.None
        DeepLink.parse("https://chefmate.plusmobileapps.com.evil.com/notifications") shouldBe
            DeepLink.None
    }

    @Test
    fun parse_profile_links() {
        DeepLink.parse("https://chefmate.plusmobileapps.com/@juliachild") shouldBe
            DeepLink.Profile("juliachild")
        DeepLink.parse("https://chefmate.plusmobileapps.com/@juliachild/") shouldBe
            DeepLink.Profile("juliachild")
        DeepLink.parse("https://chefmate.plusmobileapps.com/@juliachild?ref=share") shouldBe
            DeepLink.Profile("juliachild")
        DeepLink.parse("chefmate://profile/juliachild") shouldBe DeepLink.Profile("juliachild")
    }

    @Test
    fun parse_returns_none_for_a_bare_at_sign() {
        DeepLink.parse("https://chefmate.plusmobileapps.com/@") shouldBe DeepLink.None
        DeepLink.parse("chefmate://profile/") shouldBe DeepLink.None
    }

    @Test
    fun profile_links_from_other_hosts_are_ignored() {
        // The `@` branch must not bypass the host check that guards every other route.
        DeepLink.parse("https://evil.com/@juliachild") shouldBe DeepLink.None
        DeepLink.parse("https://chefmate.plusmobileapps.com.evil.com/@juliachild") shouldBe
            DeepLink.None
    }

    @Test
    fun a_handle_cannot_impersonate_a_route() {
        // "@settings" is a profile, not the settings screen — the namespaces never overlap.
        DeepLink.parse("https://chefmate.plusmobileapps.com/@settings") shouldBe
            DeepLink.Profile("settings")
        DeepLink.parse("https://chefmate.plusmobileapps.com/settings") shouldBe DeepLink.AppSettings
    }
}
