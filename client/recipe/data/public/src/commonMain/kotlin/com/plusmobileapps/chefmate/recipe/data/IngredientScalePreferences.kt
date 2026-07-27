package com.plusmobileapps.chefmate.recipe.data

import kotlinx.coroutines.flow.StateFlow

/** The scale factor a recipe shows when the user has never scaled it — the author's own amounts. */
const val DEFAULT_INGREDIENT_SCALE: Double = 1.0

/**
 * Remembers the ingredient scale factor (½× through 4×) the user picked for each recipe, stored
 * locally and keyed by recipe id. This is what lets a scale chosen on the recipe detail screen
 * carry over to Cook Mode — and survive leaving either screen — without ever syncing to the
 * backend.
 */
interface IngredientScalePreferences {
    /**
     * The current scale factor for [recipeId] as a hot [StateFlow], defaulting to
     * [DEFAULT_INGREDIENT_SCALE] when the recipe has never been scaled. Every flow returned for the
     * same id is backed by one shared source, so all screens showing that recipe observe the same
     * updates and stay in lock-step as the factor changes.
     */
    fun scaleFor(recipeId: Long): StateFlow<Double>

    /**
     * Persist [scale] as the chosen factor for [recipeId] and push it to that id's [scaleFor] flow.
     */
    fun setScale(recipeId: Long, scale: Double)
}
