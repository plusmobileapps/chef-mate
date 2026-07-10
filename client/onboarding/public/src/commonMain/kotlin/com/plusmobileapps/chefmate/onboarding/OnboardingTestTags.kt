package com.plusmobileapps.chefmate.onboarding

/** Stable test tags for the onboarding screens, used by snapshot and robot UI tests. */
object OnboardingTestTags {
    // Shared top nav bar (rendered by the onboarding root, present on every step).
    const val NAV_BACK_BUTTON = "onboarding_nav_back_button"
    const val NAV_SKIP_BUTTON = "onboarding_nav_skip_button"
    const val NAV_PROGRESS = "onboarding_nav_progress"

    const val WELCOME_SCREEN = "onboarding_welcome_screen"
    const val WELCOME_GET_STARTED_BUTTON = "onboarding_welcome_get_started_button"
    const val WELCOME_SIGN_IN_BUTTON = "onboarding_welcome_sign_in_button"

    const val SAVE_RECIPES_SCREEN = "onboarding_save_recipes_screen"
    const val SAVE_RECIPES_NEXT_BUTTON = "onboarding_save_recipes_next_button"

    const val COOK_MODE_SCREEN = "onboarding_cook_mode_screen"
    const val COOK_MODE_NEXT_BUTTON = "onboarding_cook_mode_next_button"

    const val GROCERY_LIST_SCREEN = "onboarding_grocery_list_screen"
    const val GROCERY_LIST_NEXT_BUTTON = "onboarding_grocery_list_next_button"

    const val MEAL_PLANNING_SCREEN = "onboarding_meal_planning_screen"
    const val MEAL_PLANNING_NEXT_BUTTON = "onboarding_meal_planning_next_button"

    const val START_COOKING_SCREEN = "onboarding_start_cooking_screen"
    const val START_COOKING_BUTTON = "onboarding_start_cooking_button"
    const val START_COOKING_SIGN_UP_BUTTON = "onboarding_start_cooking_sign_up_button"
}
