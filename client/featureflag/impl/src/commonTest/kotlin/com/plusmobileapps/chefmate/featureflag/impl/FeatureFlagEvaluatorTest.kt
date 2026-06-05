@file:Suppress("FunctionName")

package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.Platform
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.StringFlag
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class FeatureFlagEvaluatorTest {

    private val evaluator = FeatureFlagEvaluator()

    private object SampleBoolean : BooleanFlag("test_bool", defaultValue = false, "")

    private object SampleString : StringFlag("test_string", defaultValue = "default", "")

    private fun ctx(identity: String = "user-1", versionName: String = "1.4.3"): EvaluationContext =
        EvaluationContext(
            identity = identity,
            platform = Platform.ANDROID,
            versionName = versionName,
        )

    @Test
    fun When_row_is_null_Then_default_returned() {
        evaluator.evaluate(SampleBoolean, row = null, context = ctx()) shouldBe false
        evaluator.evaluate(SampleString, row = null, context = ctx()) shouldBe "default"
    }

    @Test
    fun When_row_disabled_Then_default_returned() {
        val row = booleanRow(enabled = false, value = "true", rolloutPercent = 100)
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe false
    }

    @Test
    fun When_rollout_zero_Then_default_returned() {
        val row = booleanRow(enabled = true, value = "true", rolloutPercent = 0)
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe false
    }

    @Test
    fun When_rollout_full_Then_value_returned() {
        val row = booleanRow(enabled = true, value = "true", rolloutPercent = 100)
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe true
    }

    @Test
    fun When_same_identity_Then_bucket_is_stable() {
        val row = booleanRow(enabled = true, value = "true", rolloutPercent = 50)
        val first = evaluator.evaluate(SampleBoolean, row, ctx(identity = "stable-id"))
        val second = evaluator.evaluate(SampleBoolean, row, ctx(identity = "stable-id"))
        first shouldBe second
    }

    @Test
    fun When_platform_excluded_Then_default_returned() {
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 100,
                platforms = listOf("IOS"),
            )
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe false
    }

    @Test
    fun When_platform_in_allowlist_Then_value_returned() {
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 100,
                platforms = listOf("ANDROID", "IOS"),
            )
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe true
    }

    @Test
    fun When_below_min_version_Then_default_returned() {
        val row =
            booleanRow(enabled = true, value = "true", rolloutPercent = 100, minVersion = "1.5.0")
        evaluator.evaluate(SampleBoolean, row, ctx(versionName = "1.4.3")) shouldBe false
    }

    @Test
    fun When_above_max_version_Then_default_returned() {
        val row =
            booleanRow(enabled = true, value = "true", rolloutPercent = 100, maxVersion = "1.4.0")
        evaluator.evaluate(SampleBoolean, row, ctx(versionName = "1.4.3")) shouldBe false
    }

    @Test
    fun When_within_version_range_Then_value_returned() {
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 100,
                minVersion = "1.0.0",
                maxVersion = "2.0.0",
            )
        evaluator.evaluate(SampleBoolean, row, ctx(versionName = "1.4.3")) shouldBe true
    }

    @Test
    fun When_user_allowlisted_Then_value_returned_even_outside_rollout() {
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 0,
                userIds = listOf("vip-user"),
            )
        evaluator.evaluate(SampleBoolean, row, ctx(identity = "vip-user")) shouldBe true
    }

    @Test
    fun When_user_not_allowlisted_Then_rollout_still_applies() {
        // rolloutPercent 0 means everyone not on the list gets the default.
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 0,
                userIds = listOf("vip-user"),
            )
        evaluator.evaluate(SampleBoolean, row, ctx(identity = "other-user")) shouldBe false
    }

    @Test
    fun When_user_allowlisted_but_row_disabled_Then_default_returned() {
        val row =
            booleanRow(
                enabled = false,
                value = "true",
                rolloutPercent = 100,
                userIds = listOf("vip-user"),
            )
        evaluator.evaluate(SampleBoolean, row, ctx(identity = "vip-user")) shouldBe false
    }

    @Test
    fun When_user_allowlisted_but_platform_excluded_Then_default_returned() {
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 100,
                platforms = listOf("IOS"),
                userIds = listOf("vip-user"),
            )
        evaluator.evaluate(SampleBoolean, row, ctx(identity = "vip-user")) shouldBe false
    }

    @Test
    fun When_user_allowlisted_but_below_min_version_Then_default_returned() {
        val row =
            booleanRow(
                enabled = true,
                value = "true",
                rolloutPercent = 100,
                minVersion = "1.5.0",
                userIds = listOf("vip-user"),
            )
        evaluator.evaluate(
            SampleBoolean,
            row,
            ctx(identity = "vip-user", versionName = "1.4.3"),
        ) shouldBe false
    }

    @Test
    fun When_value_type_mismatch_Then_default_returned() {
        val row =
            FeatureFlagRow(
                key = SampleBoolean.key,
                valueType = "string",
                value = "true",
                enabled = true,
                rolloutPercent = 100,
            )
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe false
    }

    @Test
    fun When_string_flag_active_Then_value_returned() {
        val row =
            FeatureFlagRow(
                key = SampleString.key,
                valueType = "string",
                value = "remote-copy",
                enabled = true,
                rolloutPercent = 100,
            )
        evaluator.evaluate(SampleString, row, ctx()) shouldBe "remote-copy"
    }

    @Test
    fun When_string_flag_out_of_rollout_Then_default_returned() {
        val row =
            FeatureFlagRow(
                key = SampleString.key,
                valueType = "string",
                value = "remote-copy",
                enabled = true,
                rolloutPercent = 0,
            )
        evaluator.evaluate(SampleString, row, ctx()) shouldBe "default"
    }

    @Test
    fun When_boolean_value_malformed_Then_resolved_to_false_and_default_used_only_when_default_is_false() {
        val row =
            FeatureFlagRow(
                key = SampleBoolean.key,
                valueType = "bool",
                value = "not-a-boolean",
                enabled = true,
                rolloutPercent = 100,
            )
        // SampleBoolean.defaultValue is false; "not-a-boolean" parses to false. Either way the
        // result is false, and that's the desired behavior on malformed remote data.
        evaluator.evaluate(SampleBoolean, row, ctx()) shouldBe false
    }

    private fun booleanRow(
        enabled: Boolean,
        value: String,
        rolloutPercent: Int,
        platforms: List<String>? = null,
        minVersion: String? = null,
        maxVersion: String? = null,
        userIds: List<String>? = null,
    ): FeatureFlagRow =
        FeatureFlagRow(
            key = SampleBoolean.key,
            valueType = "bool",
            value = value,
            enabled = enabled,
            rolloutPercent = rolloutPercent,
            platforms = platforms,
            minVersion = minVersion,
            maxVersion = maxVersion,
            userIds = userIds,
        )
}
