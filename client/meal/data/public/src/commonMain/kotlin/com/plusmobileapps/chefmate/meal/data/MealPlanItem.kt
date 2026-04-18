package com.plusmobileapps.chefmate.meal.data

data class MealPlanItem(
    val id: Long,
    val recipeId: Long,
    val recipeTitle: String,
    val recipeImageUrl: String?,
    val date: String,
    val mealType: MealType,
)
