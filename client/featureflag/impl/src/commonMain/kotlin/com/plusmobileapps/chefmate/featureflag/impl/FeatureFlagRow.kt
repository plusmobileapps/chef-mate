package com.plusmobileapps.chefmate.featureflag.impl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureFlagRow(
    val key: String,
    @SerialName("value_type") val valueType: String,
    val value: String,
    val enabled: Boolean,
    @SerialName("rollout_percent") val rolloutPercent: Int,
    val platforms: List<String>? = null,
    @SerialName("min_version") val minVersion: String? = null,
    @SerialName("max_version") val maxVersion: String? = null,
    @SerialName("user_ids") val userIds: List<String>? = null,
)
