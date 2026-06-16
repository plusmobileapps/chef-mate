package com.plusmobileapps.chefmate.di

/**
 * Stable identifiers for the first-run coach marks coordinated by [CoachMarkController]. Each value
 * doubles as the persistence key suffix, so do not rename them once shipped.
 */
object CoachMarkId {
    /** The cook-mode button on the recipe detail screen. */
    const val RECIPE_DETAIL_COOK_MODE: String = "recipe_detail_cook_mode"

    /** The add-to-grocery-list button on the recipe detail screen. */
    const val RECIPE_DETAIL_ADD_TO_GROCERY: String = "recipe_detail_add_to_grocery"

    /** The favorite button on the recipe detail screen. */
    const val RECIPE_DETAIL_FAVORITE: String = "recipe_detail_favorite"

    /** The add-to-meal-plan button on the recipe detail screen. */
    const val RECIPE_DETAIL_ADD_TO_MEAL_PLAN: String = "recipe_detail_add_to_meal_plan"

    /** The sync button on the grocery list screen. */
    const val GROCERY_LIST_SYNC: String = "grocery_list_sync"

    /**
     * The recipe detail coach marks in the order they should be shown to a first-time user. The
     * controller shows them one at a time, advancing as each is dismissed.
     */
    val recipeDetailSequence: List<String> =
        listOf(
            RECIPE_DETAIL_COOK_MODE,
            RECIPE_DETAIL_ADD_TO_GROCERY,
            RECIPE_DETAIL_FAVORITE,
            RECIPE_DETAIL_ADD_TO_MEAL_PLAN,
        )
}
