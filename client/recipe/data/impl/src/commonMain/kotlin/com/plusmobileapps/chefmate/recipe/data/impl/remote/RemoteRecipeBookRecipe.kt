package com.plusmobileapps.chefmate.recipe.data.impl.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire shape for a row in the Supabase `recipe_book_recipes` join table. */
@Serializable
data class RemoteRecipeBookRecipe(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("recipe_book_id") val recipeBookId: String,
)
