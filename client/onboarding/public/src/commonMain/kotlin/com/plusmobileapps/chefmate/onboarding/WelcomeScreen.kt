package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_get_started
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_message
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_sign_in
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_title
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusButtonVariant
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(bloc: WelcomeBloc, modifier: Modifier = Modifier) {
    OnboardingStepScaffold(
        screenTestTag = OnboardingTestTags.WELCOME_SCREEN,
        modifier = modifier,
        footer = {
            PlusButton(
                text = Res.string.onboarding_welcome_get_started.asTextData(),
                onClick = bloc::onGetStartedClicked,
                modifier =
                    Modifier.fillMaxWidth().testTag(OnboardingTestTags.WELCOME_GET_STARTED_BUTTON),
            )
            // Hidden when an already signed-in user re-enters the flow — they can't sign in again.
            if (bloc.showSignIn) {
                PlusButton(
                    text = Res.string.onboarding_welcome_sign_in.asTextData(),
                    variant = PlusButtonVariant.SECONDARY,
                    onClick = bloc::onSignInClicked,
                    modifier =
                        Modifier.fillMaxWidth().testTag(OnboardingTestTags.WELCOME_SIGN_IN_BUTTON),
                )
            }
        },
    ) {
        OnboardingAppIcon()
        Text(
            text = stringResource(Res.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.onboarding_welcome_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
