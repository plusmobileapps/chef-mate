package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_save_recipes
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_save_recipes_dark
import chefmate.client.onboarding.public.generated.resources.onboarding_save_recipes_message
import chefmate.client.onboarding.public.generated.resources.onboarding_save_recipes_title

@Composable
fun SaveRecipesScreen(bloc: SaveRecipesBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        title = Res.string.onboarding_save_recipes_title,
        message = Res.string.onboarding_save_recipes_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.SAVE_RECIPES_SCREEN,
        buttonTestTag = OnboardingTestTags.SAVE_RECIPES_NEXT_BUTTON,
        modifier = modifier,
        preview = {
            OnboardingPreviewImage(
                light = Res.drawable.onboarding_preview_save_recipes,
                dark = Res.drawable.onboarding_preview_save_recipes_dark,
            )
        },
    )
}
