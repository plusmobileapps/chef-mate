package com.plusmobileapps.chefmate.featureflag.impl

import com.plusmobileapps.chefmate.Platform
import com.plusmobileapps.chefmate.featureflag.BooleanFlag
import com.plusmobileapps.chefmate.featureflag.FeatureFlag
import com.plusmobileapps.chefmate.featureflag.StringFlag

data class EvaluationContext(val identity: String, val platform: Platform, val versionName: String)

class FeatureFlagEvaluator {

    fun <T : Any> evaluate(
        flag: FeatureFlag<T>,
        row: FeatureFlagRow?,
        context: EvaluationContext,
    ): T {
        if (row == null) return flag.defaultValue
        if (!row.enabled) return flag.defaultValue
        if (!platformMatches(row.platforms, context.platform)) return flag.defaultValue
        if (!versionMatches(row.minVersion, row.maxVersion, context.versionName)) {
            return flag.defaultValue
        }
        if (!isActive(flag.key, context.identity, row)) return flag.defaultValue
        return parseValue(flag, row.valueType, row.value)
    }

    private fun isActive(flagKey: String, identity: String, row: FeatureFlagRow): Boolean {
        // Allowlisted users get the flag regardless of the rollout bucket. The list holds
        // authenticated Supabase user ids, so this only takes effect for signed-in users
        // (logged-out clients bucket by a device UUID, which is never on the list).
        if (row.userIds?.contains(identity) == true) return true
        return inRollout(flagKey, identity, row.rolloutPercent)
    }

    private fun platformMatches(allowed: List<String>?, current: Platform): Boolean {
        if (allowed.isNullOrEmpty()) return true
        return allowed.any { it.equals(current.name, ignoreCase = true) }
    }

    private fun versionMatches(min: String?, max: String?, current: String): Boolean {
        if (min != null && compareSemver(current, min) < 0) return false
        if (max != null && compareSemver(current, max) > 0) return false
        return true
    }

    private fun inRollout(flagKey: String, identity: String, rolloutPercent: Int): Boolean {
        val clamped = rolloutPercent.coerceIn(0, 100)
        if (clamped <= 0) return false
        if (clamped >= 100) return true
        return bucket(flagKey, identity) < clamped
    }

    private fun <T : Any> parseValue(flag: FeatureFlag<T>, valueType: String, value: String): T {
        @Suppress("UNCHECKED_CAST")
        return when (flag) {
            is BooleanFlag -> {
                if (!valueType.equals("bool", ignoreCase = true)) flag.defaultValue
                else (value.equals("true", ignoreCase = true)) as T
            }
            is StringFlag -> {
                if (!valueType.equals("string", ignoreCase = true)) flag.defaultValue
                else value as T
            }
        }
    }
}
