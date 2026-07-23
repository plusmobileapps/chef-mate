package com.plusmobileapps.chefmate.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_recipe_books
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_recipe_books_dark
import chefmate.client.onboarding.public.generated.resources.onboarding_recipe_books_message
import chefmate.client.onboarding.public.generated.resources.onboarding_recipe_books_title

@Composable
fun RecipeBooksScreen(bloc: RecipeBooksBloc, modifier: Modifier = Modifier) {
    OnboardingInfoLayout(
        title = Res.string.onboarding_recipe_books_title,
        message = Res.string.onboarding_recipe_books_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.RECIPE_BOOKS_SCREEN,
        buttonTestTag = OnboardingTestTags.RECIPE_BOOKS_NEXT_BUTTON,
        modifier = modifier,
        preview = {
            OnboardingPreviewImage(
                light = Res.drawable.onboarding_preview_recipe_books,
                dark = Res.drawable.onboarding_preview_recipe_books_dark,
            )
        },
    )
}
