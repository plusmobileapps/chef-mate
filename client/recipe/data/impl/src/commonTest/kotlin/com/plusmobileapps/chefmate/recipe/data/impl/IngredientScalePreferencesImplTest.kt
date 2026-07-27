@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.recipe.data.impl

import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class IngredientScalePreferencesImplTest {

    private val settings = MapSettings()
    private val prefs = IngredientScalePreferencesImpl(settings)

    @Test
    fun When_never_scaled_Then_defaults_to_one() {
        prefs.scaleFor(7L).value shouldBe 1.0
    }

    @Test
    fun When_scale_set_Then_flow_updates_and_value_persists() {
        prefs.setScale(7L, 2.0)

        prefs.scaleFor(7L).value shouldBe 2.0
        // A fresh store reading the same settings sees the persisted factor.
        IngredientScalePreferencesImpl(settings).scaleFor(7L).value shouldBe 2.0
    }

    @Test
    fun When_same_recipe_requested_twice_Then_flows_share_updates() {
        val first = prefs.scaleFor(7L)
        val second = prefs.scaleFor(7L)

        prefs.setScale(7L, 4.0)

        // Backed by one shared source — a later reader sees the same value as an earlier one.
        first.value shouldBe 4.0
        second.value shouldBe 4.0
    }

    @Test
    fun When_scale_reset_to_one_Then_key_is_cleared() {
        prefs.setScale(7L, 3.0)
        prefs.setScale(7L, 1.0)

        prefs.scaleFor(7L).value shouldBe 1.0
        settings.hasKey("recipe_scale_7") shouldBe false
    }

    @Test
    fun When_two_recipes_scaled_Then_each_tracked_independently() {
        prefs.setScale(1L, 2.0)
        prefs.setScale(2L, 0.5)

        prefs.scaleFor(1L).value shouldBe 2.0
        prefs.scaleFor(2L).value shouldBe 0.5
    }
}
