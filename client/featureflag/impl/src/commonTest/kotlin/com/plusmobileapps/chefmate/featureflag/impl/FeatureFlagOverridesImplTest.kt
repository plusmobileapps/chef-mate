@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.featureflag.FeatureFlagRegistry
import com.plusmobileapps.chefmate.featureflag.Override
import com.russhwolf.settings.MapSettings
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FeatureFlagOverridesImplTest {

    @Test
    fun When_no_overrides_set_Then_default_returned_for_all_flags() {
        val overrides = FeatureFlagOverridesImpl(MapSettings())
        FeatureFlagRegistry.all.forEach { flag ->
            overrides.overrideOf(flag).value shouldBe Override.Default
        }
    }

    @Test
    fun When_boolean_override_force_on_Then_state_emits_force_value() {
        val overrides = FeatureFlagOverridesImpl(MapSettings())
        val flag = FeatureFlagRegistry.CookModeV2

        overrides.setOverride(flag, Override.ForceValue(true))

        overrides.overrideOf(flag).value shouldBe Override.ForceValue(true)
    }

    @Test
    fun When_string_override_set_Then_state_emits_force_value() {
        val overrides = FeatureFlagOverridesImpl(MapSettings())
        val flag = FeatureFlagRegistry.HomeBannerText

        overrides.setOverride(flag, Override.ForceValue("local copy"))

        overrides.overrideOf(flag).value shouldBe Override.ForceValue("local copy")
    }

    @Test
    fun When_override_cleared_Then_state_falls_back_to_default() {
        val overrides = FeatureFlagOverridesImpl(MapSettings())
        val flag = FeatureFlagRegistry.CookModeV2

        overrides.setOverride(flag, Override.ForceValue(true))
        overrides.setOverride(flag, Override.Default)

        overrides.overrideOf(flag).value shouldBe Override.Default
    }

    @Test
    fun When_overrides_persisted_Then_new_instance_loads_them() {
        val backing = MapSettings()
        FeatureFlagOverridesImpl(backing).apply {
            setOverride(FeatureFlagRegistry.CookModeV2, Override.ForceValue(true))
            setOverride(FeatureFlagRegistry.HomeBannerText, Override.ForceValue("hello"))
        }

        val reloaded = FeatureFlagOverridesImpl(backing)

        reloaded.overrideOf(FeatureFlagRegistry.CookModeV2).value shouldBe Override.ForceValue(true)
        reloaded.overrideOf(FeatureFlagRegistry.HomeBannerText).value shouldBe
            Override.ForceValue("hello")
    }

    @Test
    fun When_clearAll_Then_all_overrides_reset() {
        val overrides = FeatureFlagOverridesImpl(MapSettings())
        overrides.setOverride(FeatureFlagRegistry.CookModeV2, Override.ForceValue(true))
        overrides.setOverride(FeatureFlagRegistry.HomeBannerText, Override.ForceValue("x"))

        overrides.clearAll()

        FeatureFlagRegistry.all.forEach { flag ->
            overrides.overrideOf(flag).value shouldBe Override.Default
        }
    }

    @Test
    fun When_string_override_is_empty_Then_treated_as_force_value_not_default() {
        val overrides = FeatureFlagOverridesImpl(MapSettings())
        val flag = FeatureFlagRegistry.HomeBannerText

        overrides.setOverride(flag, Override.ForceValue(""))

        overrides.overrideOf(flag).value shouldBe Override.ForceValue("")
    }
}
