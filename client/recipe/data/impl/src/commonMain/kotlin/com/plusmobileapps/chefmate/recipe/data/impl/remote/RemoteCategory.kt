package com.plusmobileapps.chefmate.recipe.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCategory(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    @SerialName("builtin_id") val builtinId: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
