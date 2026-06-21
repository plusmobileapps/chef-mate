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

    /** The add-recipe (create/scan) menu on the recipe list screen. */
    const val RECIPE_LIST_ADD: String = "recipe_list_add"

    /** The grid/list view-mode toggle on the recipe list screen. */
    const val RECIPE_LIST_VIEW_MODE: String = "recipe_list_view_mode"

    /** The sort/filter button on the recipe list screen. */
    const val RECIPE_LIST_FILTER: String = "recipe_list_filter"

    /** The add-meal button on the meal plan screen. */
    const val MEAL_PLAN_ADD_MEAL: String = "meal_plan_add_meal"

    /** The day/week/month view-mode control on the meal plan screen. */
    const val MEAL_PLAN_VIEW_MODE: String = "meal_plan_view_mode"

    /** The search field on the in-app browser landing screen. */
    const val BROWSER_SEARCH: String = "browser_search"

    /** The rich-text mode toggle on the edit recipe screen. */
    const val EDIT_RECIPE_RICH_TEXT: String = "edit_recipe_rich_text"

    /** The save button on the edit recipe screen. */
    const val EDIT_RECIPE_SAVE: String = "edit_recipe_save"

    /** The keep-screen-on (always-on display) toggle on the cook mode screen. */
    const val COOK_MODE_KEEP_SCREEN_ON: String = "cook_mode_keep_screen_on"

    /** The stacked/split layout (view mode) toggle on the cook mode screen. */
    const val COOK_MODE_LAYOUT: String = "cook_mode_layout"

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

    /** The recipe list coach marks, shown one at a time in order. */
    val recipeListSequence: List<String> =
        listOf(RECIPE_LIST_ADD, RECIPE_LIST_VIEW_MODE, RECIPE_LIST_FILTER)

    /** The meal plan coach marks, shown one at a time in order. */
    val mealPlanSequence: List<String> = listOf(MEAL_PLAN_ADD_MEAL, MEAL_PLAN_VIEW_MODE)

    /** The browser coach marks, shown one at a time in order. */
    val browserSequence: List<String> = listOf(BROWSER_SEARCH)

    /** The edit recipe coach marks, shown one at a time in order. */
    val editRecipeSequence: List<String> = listOf(EDIT_RECIPE_RICH_TEXT, EDIT_RECIPE_SAVE)

    /** The cook mode coach marks, shown one at a time in order. */
    val cookModeSequence: List<String> = listOf(COOK_MODE_KEEP_SCREEN_ON, COOK_MODE_LAYOUT)
}
