package com.plusmobileapps.chefmate.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_save_recipes_message
import chefmate.client.onboarding.public.generated.resources.onboarding_save_recipes_title

@Composable
fun SaveRecipesScreen(bloc: SaveRecipesBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        icon = Icons.Default.FileDownload,
        title = Res.string.onboarding_save_recipes_title,
        message = Res.string.onboarding_save_recipes_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.SAVE_RECIPES_SCREEN,
        buttonTestTag = OnboardingTestTags.SAVE_RECIPES_NEXT_BUTTON,
        modifier = modifier,
    )
}
