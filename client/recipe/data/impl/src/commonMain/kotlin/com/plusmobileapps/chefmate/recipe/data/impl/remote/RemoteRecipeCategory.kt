package com.plusmobileapps.chefmate.recipe.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire shape for a row in the Supabase `recipe_categories` join table. */
@Serializable
data class RemoteRecipeCategory(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("category_id") val categoryId: String,
)
