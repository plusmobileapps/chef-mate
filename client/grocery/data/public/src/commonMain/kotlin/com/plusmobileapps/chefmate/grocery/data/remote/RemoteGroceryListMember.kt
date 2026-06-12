package com.plusmobileapps.chefmate.grocery.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteGroceryListMember(
    val id: String? = null,
    @SerialName("list_id") val listId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("invited_email") val invitedEmail: String,
    val role: String = "editor",
    val status: String = "pending",
    @SerialName("invited_by") val invitedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class RemoteGroceryListInvite(
    @SerialName("member_id") val id: String,
    @SerialName("list_id") val listId: String,
    @SerialName("list_name") val listName: String,
    val role: String,
    val status: String,
)

@Serializable
data class RemoteGroceryListCollaborator(
    @SerialName("member_id") val memberId: String?,
    val email: String,
    val name: String?,
    val role: String,
    val status: String,
    @SerialName("is_owner") val isOwner: Boolean,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
