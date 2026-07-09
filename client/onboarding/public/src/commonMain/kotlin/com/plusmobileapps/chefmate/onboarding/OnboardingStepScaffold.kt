package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainerDefaults
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/**
 * Shared layout for every onboarding step: the informational [content] scrolls and stays vertically
 * centered in the space beneath the nav bar, while [footer] (the primary action) is pinned to the
 * bottom of the screen above the system navigation bar. Content is capped at
 * [PlusHeaderContainerDefaults.MaxContentWidth] and centered so it reads well on wide windows.
 */
@Composable
internal fun OnboardingStepScaffold(
    screenTestTag: String,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = ChefMateTheme.dimens
    val maxWidth = PlusHeaderContainerDefaults.MaxContentWidth

    Column(
        modifier =
            modifier
                .testTag(screenTestTag)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth().widthIn(max = maxWidth).padding(dimens.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
        }

        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .widthIn(max = maxWidth)
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .padding(horizontal = dimens.paddingLarge)
                    .padding(bottom = dimens.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(dimens.paddingSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            footer()
        }
    }
}
