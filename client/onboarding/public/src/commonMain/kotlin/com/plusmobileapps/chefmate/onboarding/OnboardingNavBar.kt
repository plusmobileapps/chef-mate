package com.plusmobileapps.chefmate.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import chefmate.client.onboarding.public.generated.resources.Res
import chefmate.client.onboarding.public.generated.resources.onboarding_back
import chefmate.client.onboarding.public.generated.resources.onboarding_skip
import com.plusmobileapps.chefmate.ui.components.PlusIconButton
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Top-anchored chrome for the onboarding flow: a back arrow (hidden on the first step), a centered
 * step-progress indicator, and a Skip action. The onboarding root owns navigation state, so it
 * drives this bar — [currentStep] is the zero-based index of the active step and [onBackClick] is
 * null when there's nowhere to go back to.
 */
@Composable
fun OnboardingNavBar(
    currentStep: Int,
    totalSteps: Int,
    onBackClick: (() -> Unit)?,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dimens = ChefMateTheme.dimens
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                        .union(WindowInsets.displayCutout)
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                )
                .height(dimens.rowHeight)
                .padding(horizontal = dimens.paddingSmall)
    ) {
        if (onBackClick != null) {
            PlusIconButton(
                modifier =
                    Modifier.align(Alignment.CenterStart)
                        .testTag(OnboardingTestTags.NAV_BACK_BUTTON),
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.onboarding_back),
                onClick = onBackClick,
            )
        }

        OnboardingProgressIndicator(
            currentStep = currentStep,
            totalSteps = totalSteps,
            modifier = Modifier.align(Alignment.Center),
        )

        TextButton(
            onClick = onSkipClick,
            modifier =
                Modifier.align(Alignment.CenterEnd).testTag(OnboardingTestTags.NAV_SKIP_BUTTON),
        ) {
            Text(stringResource(Res.string.onboarding_skip))
        }
    }
}

/**
 * A row of dots — one per step — with the active step rendered as a wider pill. Purely decorative,
 * so it's hidden from accessibility (the back/skip controls and screen content convey position).
 */
@Composable
private fun OnboardingProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {}.testTag(OnboardingTestTags.NAV_PROGRESS),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalSteps) { index ->
            val active = index == currentStep
            val color by
                animateColorAsState(
                    if (active) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
            val width by animateDpAsState(if (active) 24.dp else 8.dp)
            Box(modifier = Modifier.height(8.dp).width(width).clip(CircleShape).background(color))
        }
    }
}
