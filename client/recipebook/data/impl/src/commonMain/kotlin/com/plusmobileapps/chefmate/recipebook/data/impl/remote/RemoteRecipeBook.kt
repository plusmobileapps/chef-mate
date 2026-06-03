package com.plusmobileapps.chefmate.recipebook.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteRecipeBook(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    @SerialName("is_default") val isDefault: Boolean = false,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
