package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_grocery_lists_message
import chefmate.client.onboarding.public.generated.resources.onboarding_grocery_lists_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_grocery_lists
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_grocery_lists_dark

@Composable
fun GroceryListsScreen(bloc: GroceryListsBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        title = Res.string.onboarding_grocery_lists_title,
        message = Res.string.onboarding_grocery_lists_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.GROCERY_LISTS_SCREEN,
        buttonTestTag = OnboardingTestTags.GROCERY_LISTS_NEXT_BUTTON,
        modifier = modifier,
        preview = {
            OnboardingPreviewImage(
                light = Res.drawable.onboarding_preview_grocery_lists,
                dark = Res.drawable.onboarding_preview_grocery_lists_dark,
            )
        },
    )
}
