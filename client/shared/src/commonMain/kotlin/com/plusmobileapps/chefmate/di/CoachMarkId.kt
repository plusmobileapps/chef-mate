package com.plusmobileapps.chefmate.di

/**
 * Stable identifiers for the first-run coach marks coordinated by [CoachMarkController]. Each value
 * doubles as the persistence key suffix, so do not rename them once shipped.
 */
object CoachMarkId {
    /** The cook-mode button on the recipe detail screen. */
    const val RECIPE_DETAIL_COOK_MODE: String = "recipe_detail_cook_mode"

    /** The sync button on the grocery list screen. */
    const val GROCERY_LIST_SYNC: String = "grocery_list_sync"
}
