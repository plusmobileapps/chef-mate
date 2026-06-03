package com.plusmobileapps.chefmate.recipe.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteRecipe(
    val id: String? = null,
    @SerialName("owner_id") val ownerId: String,
    val title: String,
    val description: String? = null,
    val ingredients: String? = null,
    val directions: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("source_url") val sourceUrl: String? = null,
    val servings: Int? = null,
    @SerialName("prep_time") val prepTime: Int? = null,
    @SerialName("cook_time") val cookTime: Int? = null,
    @SerialName("total_time") val totalTime: Int? = null,
    val calories: Int? = null,
    @SerialName("star_rating") val starRating: Int? = null,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("recipe_book_id") val recipeBookId: String? = null,
)
