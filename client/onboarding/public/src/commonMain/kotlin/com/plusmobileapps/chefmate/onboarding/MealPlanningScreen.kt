package com.plusmobileapps.chefmate.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_meal_planning_message
import chefmate.client.onboarding.public.generated.resources.onboarding_meal_planning_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next

@Composable
fun MealPlanningScreen(bloc: MealPlanningBloc, modifier: Modifier = Modifier) {
    OnboardingIconLayout(
        icon = Icons.Default.CalendarMonth,
        title = Res.string.onboarding_meal_planning_title,
        message = Res.string.onboarding_meal_planning_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.MEAL_PLANNING_SCREEN,
        buttonTestTag = OnboardingTestTags.MEAL_PLANNING_NEXT_BUTTON,
        modifier = modifier,
    )
}
