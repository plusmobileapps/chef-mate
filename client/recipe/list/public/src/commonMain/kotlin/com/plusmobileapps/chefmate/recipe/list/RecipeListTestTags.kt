package com.plusmobileapps.chefmate.recipe.list

/**
 * Stable test tags applied to the recipe list screen. Shared with `RecipeListRobot` so UI tests can
 * scope semantics-tree lookups to this screen and avoid colliding with identical text rendered
 * elsewhere (e.g. a recipe title shown on the detail screen header).
 */
object RecipeListTestTags {
    const val SCREEN: String = "recipe_list_screen"
}
