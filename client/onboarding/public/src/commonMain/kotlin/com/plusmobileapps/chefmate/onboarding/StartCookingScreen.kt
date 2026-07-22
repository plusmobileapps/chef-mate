package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_start_cooking_button
import chefmate.client.onboarding.public.generated.resources.onboarding_start_cooking_message
import chefmate.client.onboarding.public.generated.resources.onboarding_start_cooking_sign_up
import chefmate.client.onboarding.public.generated.resources.onboarding_start_cooking_title
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import org.jetbrains.compose.resources.stringResource

@Composable
fun StartCookingScreen(bloc: StartCookingBloc, modifier: Modifier = Modifier) {
    OnboardingStepScaffold(
        screenTestTag = OnboardingTestTags.START_COOKING_SCREEN,
        modifier = modifier,
        footer = {
            // Hidden when an already signed-in user re-enters the flow — they can't sign up again.
            if (bloc.showSignUp) {
                PlusButton(
                    text = Res.string.onboarding_start_cooking_sign_up.asTextData(),
                    onClick = bloc::onSignUpClicked,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(OnboardingTestTags.START_COOKING_SIGN_UP_BUTTON),
                )
            }
            PlusButton(
                text = Res.string.onboarding_start_cooking_button.asTextData(),
                variant = PlusButtonVariant.SECONDARY,
                onClick = bloc::onStartCookingClicked,
                modifier = Modifier.fillMaxWidth().testTag(OnboardingTestTags.START_COOKING_BUTTON),
            )
        },
    ) {
        OnboardingAppIcon()
        Icon(
            imageVector = Icons.Default.Celebration,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp),
        )
        Text(
            text = stringResource(Res.string.onboarding_start_cooking_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.onboarding_start_cooking_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
