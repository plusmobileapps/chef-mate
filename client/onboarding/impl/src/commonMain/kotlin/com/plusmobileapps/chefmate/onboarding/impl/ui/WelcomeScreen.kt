package com.plusmobileapps.chefmate.onboarding.impl.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_get_started
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_message
import chefmate.client.onboarding.public.generated.resources.onboarding_welcome_title
import com.plusmobileapps.chefmate.onboarding.OnboardingTestTags
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.chefmate.text.asTextData
import com.plusmobileapps.chefmate.ui.components.PlusButton
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainer
import com.plusmobileapps.chefmate.ui.components.PlusHeaderData
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeScreen(bloc: WelcomeBloc, modifier: Modifier = Modifier) {
    val dimens = ChefMateTheme.dimens

    PlusHeaderContainer(
        modifier = modifier.testTag(OnboardingTestTags.WELCOME_SCREEN).fillMaxWidth(),
        data = PlusHeaderData.None,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
            PlusButton(
                text = Res.string.onboarding_welcome_get_started.asTextData(),
                onClick = bloc::onGetStartedClicked,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = dimens.paddingNormal)
                        .testTag(OnboardingTestTags.WELCOME_GET_STARTED_BUTTON),
            )
        }
    }
}
