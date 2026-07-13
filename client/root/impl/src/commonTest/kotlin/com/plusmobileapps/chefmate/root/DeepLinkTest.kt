@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.root

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
    fun parse_recipe_detail_returns_none_when_id_is_missing_or_invalid() {
        DeepLink.parse("chefmate://recipe") shouldBe DeepLink.None
        DeepLink.parse("chefmate://recipe/") shouldBe DeepLink.None
        DeepLink.parse("chefmate://recipe/abc") shouldBe DeepLink.None
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
}
