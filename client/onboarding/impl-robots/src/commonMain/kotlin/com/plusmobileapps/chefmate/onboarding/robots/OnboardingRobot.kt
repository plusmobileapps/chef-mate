@file:OptIn(ExperimentalTestApi::class)

package com.plusmobileapps.chefmate.onboarding.robots

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import com.plusmobileapps.chefmate.onboarding.OnboardingTestTags

/**
 * Robot for the onboarding flow. Each step is matched by its screen root test tag, and each action
 * clicks the step's button by tag, so the test reads as the user walking through the tour.
 */
class OnboardingRobot(private val test: ComposeUiTest) {

    fun assertWelcomeDisplayed(): OnboardingRobot =
        assertDisplayed(OnboardingTestTags.WELCOME_SCREEN)

    fun clickGetStarted(): OnboardingRobot = click(OnboardingTestTags.WELCOME_GET_STARTED_BUTTON)

    fun clickSignIn(): OnboardingRobot = click(OnboardingTestTags.WELCOME_SIGN_IN_BUTTON)

    /** Skip lives in the shared top nav bar, so it's available from every step. */
    fun clickSkip(): OnboardingRobot = click(OnboardingTestTags.NAV_SKIP_BUTTON)

    /** Back lives in the shared top nav bar; hidden on the first (Welcome) step. */
    fun clickBack(): OnboardingRobot = click(OnboardingTestTags.NAV_BACK_BUTTON)

    fun assertSaveRecipesDisplayed(): OnboardingRobot =
        assertDisplayed(OnboardingTestTags.SAVE_RECIPES_SCREEN)

    fun clickSaveRecipesNext(): OnboardingRobot = click(OnboardingTestTags.SAVE_RECIPES_NEXT_BUTTON)

    fun assertCookModeDisplayed(): OnboardingRobot =
        assertDisplayed(OnboardingTestTags.COOK_MODE_SCREEN)

    fun clickCookModeNext(): OnboardingRobot = click(OnboardingTestTags.COOK_MODE_NEXT_BUTTON)

    fun assertGroceryListDisplayed(): OnboardingRobot =
        assertDisplayed(OnboardingTestTags.GROCERY_LIST_SCREEN)

    fun clickGroceryListNext(): OnboardingRobot = click(OnboardingTestTags.GROCERY_LIST_NEXT_BUTTON)

    fun assertMealPlanningDisplayed(): OnboardingRobot =
        assertDisplayed(OnboardingTestTags.MEAL_PLANNING_SCREEN)

    fun clickMealPlanningNext(): OnboardingRobot =
        click(OnboardingTestTags.MEAL_PLANNING_NEXT_BUTTON)

    fun assertStartCookingDisplayed(): OnboardingRobot =
        assertDisplayed(OnboardingTestTags.START_COOKING_SCREEN)

    fun clickStartCooking(): OnboardingRobot = click(OnboardingTestTags.START_COOKING_BUTTON)

    private fun assertDisplayed(tag: String): OnboardingRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(tag))
    }

    private fun click(tag: String): OnboardingRobot = apply {
        test.waitUntilExactlyOneExists(hasTestTag(tag))
        test.onNodeWithTag(tag).performClick()
    }
}

fun ComposeUiTest.onboarding(): OnboardingRobot = OnboardingRobot(this)
