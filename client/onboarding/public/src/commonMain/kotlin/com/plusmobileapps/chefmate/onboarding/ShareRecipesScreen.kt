package com.plusmobileapps.chefmate.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_next
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_share_android
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_share_android_dark
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_share_ios
import chefmate.client.onboarding.public.generated.resources.onboarding_preview_share_ios_dark
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
 * (no share sheet), so it keeps the icon layout; iOS and Android each show a capture of their own
 * system share sheet, which look different enough to warrant a per-platform image.
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
        val (light, dark) =
            when (platform) {
                Platform.IOS ->
                    Res.drawable.onboarding_preview_share_ios to
                        Res.drawable.onboarding_preview_share_ios_dark
                else ->
                    Res.drawable.onboarding_preview_share_android to
                        Res.drawable.onboarding_preview_share_android_dark
            }
        OnboardingInfoLayout(
            title = Res.string.onboarding_share_recipes_title,
            message = Res.string.onboarding_share_recipes_message_mobile,
            buttonText = Res.string.onboarding_next,
            onButtonClick = onNextClick,
            screenTestTag = OnboardingTestTags.SHARE_RECIPES_SCREEN,
            buttonTestTag = OnboardingTestTags.SHARE_RECIPES_NEXT_BUTTON,
            modifier = modifier,
            preview = { OnboardingPreviewImage(light = light, dark = dark) },
        )
    }
}
