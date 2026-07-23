package com.plusmobileapps.chefmate.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_share_recipes_message_desktop
import chefmate.client.onboarding.public.generated.resources.onboarding_share_recipes_message_mobile
import chefmate.client.onboarding.public.generated.resources.onboarding_share_recipes_title
import com.plusmobileapps.chefmate.Platform
import com.plusmobileapps.chefmate.currentPlatform

@Composable
fun ShareRecipesScreen(bloc: ShareRecipesBloc, modifier: Modifier = Modifier) {
    // Desktop copies the URL from the address bar; mobile shares the page to ChefMate directly.
    ShareRecipesScreen(
        isDesktop = currentPlatform == Platform.JVM,
        onNextClick = bloc::onNextClicked,
        modifier = modifier,
    )
}

/**
 * Platform-parameterized body, split out so both the mobile and desktop variants can be previewed
 * and snapshot-tested independently of the host platform.
 */
@Composable
fun ShareRecipesScreen(
    isDesktop: Boolean,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Desktop has no share-sheet screenshot to show (it copies the URL from the address bar), so it
    // keeps the icon layout; mobile shows the real browser share sheet capture.
    if (isDesktop) {
        OnboardingIconLayout(
            icon = Icons.Default.ContentCopy,
            title = Res.string.onboarding_share_recipes_title,
            message = Res.string.onboarding_share_recipes_message_desktop,
            buttonText = Res.string.onboarding_next,
            onButtonClick = onNextClick,
            screenTestTag = OnboardingTestTags.SHARE_RECIPES_SCREEN,
            buttonTestTag = OnboardingTestTags.SHARE_RECIPES_NEXT_BUTTON,
            modifier = modifier,
        )
    } else {
        OnboardingInfoLayout(
            title = Res.string.onboarding_share_recipes_title,
            message = Res.string.onboarding_share_recipes_message_mobile,
            buttonText = Res.string.onboarding_next,
            onButtonClick = onNextClick,
            screenTestTag = OnboardingTestTags.SHARE_RECIPES_SCREEN,
            buttonTestTag = OnboardingTestTags.SHARE_RECIPES_NEXT_BUTTON,
            modifier = modifier,
            preview = {
                OnboardingGifPreview(
                    uri = Res.getUri("files/onboarding_share_android.gif"),
                    aspectRatio = 281f / 500f, // the capture's native size
                )
            },
        )
    }
}
