package com.plusmobileapps.chefmate.recipe.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteRecipeShare(
    val id: String? = null,
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("invited_email") val invitedEmail: String,
    val role: String = "viewer",
    val status: String = "pending",
    @SerialName("invited_by") val invitedBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
