package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_grocery_list_message
import chefmate.client.onboarding.public.generated.resources.onboarding_grocery_list_title
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_grocery_list
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_grocery_list_dark

@Composable
fun GroceryListScreen(bloc: GroceryListBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        previewLight = Res.drawable.onboarding_preview_grocery_list,
        previewDark = Res.drawable.onboarding_preview_grocery_list_dark,
        title = Res.string.onboarding_grocery_list_title,
        message = Res.string.onboarding_grocery_list_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.GROCERY_LIST_SCREEN,
        buttonTestTag = OnboardingTestTags.GROCERY_LIST_NEXT_BUTTON,
        modifier = modifier,
    )
}
