package com.plusmobileapps.chefmate.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_cook_mode_message
import chefmate.client.onboarding.public.generated.resources.onboarding_cook_mode_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next

@Composable
fun CookModeScreen(bloc: CookModeBloc, modifier: Modifier = Modifier) {
    OnboardingIconLayout(
        icon = Icons.Default.SoupKitchen,
        title = Res.string.onboarding_cook_mode_title,
        message = Res.string.onboarding_cook_mode_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.COOK_MODE_SCREEN,
        buttonTestTag = OnboardingTestTags.COOK_MODE_NEXT_BUTTON,
        modifier = modifier,
    )
}
