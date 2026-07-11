package com.plusmobileapps.chefmate.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_recipe_books_message
import chefmate.client.onboarding.public.generated.resources.onboarding_recipe_books_title

@Composable
fun RecipeBooksScreen(bloc: RecipeBooksBloc, modifier: Modifier = Modifier) {
    OnboardingIconLayout(
        icon = Icons.AutoMirrored.Filled.MenuBook,
        title = Res.string.onboarding_recipe_books_title,
        message = Res.string.onboarding_recipe_books_message,
        buttonText = Res.string.onboarding_next,
        onButtonClick = bloc::onNextClicked,
        screenTestTag = OnboardingTestTags.RECIPE_BOOKS_SCREEN,
        buttonTestTag = OnboardingTestTags.RECIPE_BOOKS_NEXT_BUTTON,
        modifier = modifier,
    )
}
