package com.plusmobileapps.chefmate.recipe.list

data class RecipeListItem(
    val id: Long,
    val title: String,
    val description: String?,
    val imageUrl: String?,
    val starRating: Int?,
)
