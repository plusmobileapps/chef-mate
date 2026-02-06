package com.plusmobileapps.chefmate.grocery.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteGroceryItem(
    val id: String? = null,
    @SerialName("list_id") val listId: String,
    val name: String,
    @SerialName("is_checked") val isChecked: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class RemoteGroceryList(
    val id: String? = null,
    val name: String = "My Grocery List",
    @SerialName("owner_id") val ownerId: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
