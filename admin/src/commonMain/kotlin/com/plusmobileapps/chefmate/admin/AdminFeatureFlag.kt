package com.plusmobileapps.chefmate.admin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Full `feature_flags` row as seen by the admin tool, including the admin-only columns (`archived`,
 * `description`, timestamps) that the shipping client never reads. Field names mirror the client's
 * `FeatureFlagRow` so the wire format stays identical.
 */
@Serializable
data class AdminFeatureFlag(
    val key: String,
    @SerialName("value_type") val valueType: String,
    val value: String,
    val enabled: Boolean,
    @SerialName("rollout_percent") val rolloutPercent: Int = 100,
    val platforms: List<String>? = null,
    @SerialName("min_version") val minVersion: String? = null,
    @SerialName("max_version") val maxVersion: String? = null,
    // Allowlist of Supabase user ids that get the flag regardless of the rollout bucket (see PR
    // #249). Additive on top of `rollout_percent`; still subject to enabled/platforms/version
    // gates.
    @SerialName("user_ids") val userIds: List<String>? = null,
    val archived: Boolean = false,
    val description: String? = null,
) {
    companion object {
        const val TYPE_BOOL = "bool"
        const val TYPE_STRING = "string"
    }
}
