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
        DeepLink.parse("chefmate://signin") shouldBe DeepLink.SignIn
        DeepLink.parse("chefmate://signup") shouldBe DeepLink.SignUp
    }

    @Test
    fun parse_tolerates_trailing_slash() {
        DeepLink.parse("chefmate://groceries/") shouldBe DeepLink.Groceries
        DeepLink.parse("chefmate://recipe/42/") shouldBe DeepLink.RecipeDetail(42L)
    }
}
