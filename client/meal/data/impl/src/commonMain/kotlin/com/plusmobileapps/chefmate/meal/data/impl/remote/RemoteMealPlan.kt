package com.plusmobileapps.chefmate.meal.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteMealPlan(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    @SerialName("recipe_id") val recipeId: Long,
    val date: String,
    @SerialName("meal_type") val mealType: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("client_id") val clientId: String? = null,
)
