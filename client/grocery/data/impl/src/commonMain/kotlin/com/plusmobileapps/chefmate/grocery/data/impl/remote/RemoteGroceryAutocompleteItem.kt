package com.plusmobileapps.chefmate.grocery.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteGroceryAutocompleteItem(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
