package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_cook_mode_message
import chefmate.client.onboarding.public.generated.resources.onboarding_cook_mode_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_cook_mode
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_cook_mode_dark

@Composable
fun CookModeScreen(bloc: CookModeBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        title = Res.string.onboarding_cook_mode_title,
        message = Res.string.onboarding_cook_mode_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.COOK_MODE_SCREEN,
        buttonTestTag = OnboardingTestTags.COOK_MODE_NEXT_BUTTON,
        modifier = modifier,
        preview = {
            OnboardingPreviewImage(
                light = Res.drawable.onboarding_preview_cook_mode,
                dark = Res.drawable.onboarding_preview_cook_mode_dark,
            )
        },
    )
}
