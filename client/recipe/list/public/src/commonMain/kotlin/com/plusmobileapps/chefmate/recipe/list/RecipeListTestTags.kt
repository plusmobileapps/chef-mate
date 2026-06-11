package com.plusmobileapps.chefmate.recipe.list

/**
 * Stable test tags applied to the recipe list screen. Shared with `RecipeListRobot` so UI tests can
 * scope semantics-tree lookups to this screen and avoid colliding with identical text rendered
 * elsewhere (e.g. a recipe title shown on the detail screen header).
 */
object RecipeListTestTags {
    const val SCREEN: String = "recipe_list_screen"
    const val ADD_RECIPE_BUTTON: String = "recipe_list_add_recipe_button"
    const val ADD_MENU_CREATE: String = "recipe_list_add_menu_create"
    const val ADD_MENU_SCAN: String = "recipe_list_add_menu_scan"
    const val SCANNING_DIALOG: String = "recipe_list_scanning_dialog"
    const val SCAN_ERROR_DIALOG: String = "recipe_list_scan_error_dialog"
    const val BOOK_SELECTOR: String = "recipe_list_book_selector"
    const val SEARCH_BUTTON: String = "recipe_list_search_button"
    const val SEARCH_SHEET: String = "recipe_list_search_sheet"
    const val SEARCH_FIELD: String = "recipe_list_search_field"
    const val SEARCH_SCOPE_ALL: String = "recipe_list_search_scope_all"
    const val SEARCH_CLEAR: String = "recipe_list_search_clear"
    const val SEARCH_DONE: String = "recipe_list_search_done"
}
