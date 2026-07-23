package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_add_to_grocery_message
import chefmate.client.onboarding.public.generated.resources.onboarding_add_to_grocery_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_add_to_grocery
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_add_to_grocery_dark

@Composable
fun AddToGroceryScreen(bloc: AddToGroceryBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        title = Res.string.onboarding_add_to_grocery_title,
        message = Res.string.onboarding_add_to_grocery_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.ADD_TO_GROCERY_SCREEN,
        buttonTestTag = OnboardingTestTags.ADD_TO_GROCERY_NEXT_BUTTON,
        modifier = modifier,
        preview = {
            OnboardingPreviewImage(
                light = Res.drawable.onboarding_preview_add_to_grocery,
                dark = Res.drawable.onboarding_preview_add_to_grocery_dark,
            )
        },
    )
}
