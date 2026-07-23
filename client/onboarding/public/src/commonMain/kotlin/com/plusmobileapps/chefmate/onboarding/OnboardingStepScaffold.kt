package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import com.plusmobileapps.chefmate.ui.components.PlusHeaderContainerDefaults
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/**
 * Shared layout for every onboarding step: the informational [content] fills the space beneath the
 * nav bar and scrolls, staying vertically centered when it fits; the [footer] (the primary action)
 * floats over the bottom of that content so the content scrolls behind it. The content reserves
 * exactly the footer's height as bottom padding, so when a step is tall enough to scroll, its last
 * rows can scroll clear of the floating footer instead of staying hidden underneath. Content and
 * footer are both capped at [PlusHeaderContainerDefaults.MaxContentWidth] and centered so they read
 * well on wide windows.
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

    // The footer floats over the content rather than taking its own row, so reserve exactly its
    // measured height as content bottom padding — that's the "space to scroll above the button".
    var footerHeightPx by remember { mutableStateOf(0) }
    val footerHeight = with(LocalDensity.current) { footerHeightPx.toDp() }

    Box(
        modifier =
            modifier
                .testTag(screenTestTag)
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .widthIn(max = maxWidth)
                        .padding(dimens.paddingLarge)
                        .padding(bottom = footerHeight),
                verticalArrangement = Arrangement.spacedBy(dimens.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                content()
            }
        }

        Column(
            modifier =
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .widthIn(max = maxWidth)
                    .onSizeChanged { footerHeightPx = it.height }
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
