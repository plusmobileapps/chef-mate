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
    ShareRecipesScreen(
        platform = currentPlatform,
        onNextClick = bloc::onNextClicked,
        modifier = modifier,
    )
}

/**
 * Platform-parameterized body, split out so each platform variant can be previewed and
 * snapshot-tested independently of the host platform. Desktop copies the URL from the address bar
 * (no share sheet), so it keeps the icon layout; iOS and Android each show their own share-sheet
 * gif because the flow — and the sheet chrome — differs between them.
 */
@Composable
fun ShareRecipesScreen(
    platform: Platform,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (platform == Platform.JVM) {
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
        // Each gif's aspect ratio is its own native capture size (width / height).
        val (gifPath, gifAspectRatio) =
            when (platform) {
                Platform.IOS -> "files/onboarding_share_ios.gif" to 480f / 1044f
                else -> "files/onboarding_share_android.gif" to 480f / 1067f
            }
        OnboardingInfoLayout(
            title = Res.string.onboarding_share_recipes_title,
            message = Res.string.onboarding_share_recipes_message_mobile,
            buttonText = Res.string.onboarding_next,
            onButtonClick = onNextClick,
            screenTestTag = OnboardingTestTags.SHARE_RECIPES_SCREEN,
            buttonTestTag = OnboardingTestTags.SHARE_RECIPES_NEXT_BUTTON,
            modifier = modifier,
            preview = {
                OnboardingGifPreview(uri = Res.getUri(gifPath), aspectRatio = gifAspectRatio)
            },
        )
    }
}
