package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

@Composable
fun OnboardingRootScreen(bloc: OnboardingRootBloc, modifier: Modifier = Modifier) {
    val stack by bloc.routerState.subscribeAsState()
    // The router only ever holds the steps visited so far, so its depth is the current position.
    val currentStep = stack.items.lastIndex

    Column(modifier = modifier.fillMaxSize().background(ChefMateTheme.colorScheme.background)) {
        OnboardingNavBar(
            currentStep = currentStep,
            totalSteps = bloc.totalSteps,
            // On the first step there's no previous step to return to, so the arrow is hidden —
            // unless the flow was re-entered from within the app, where it backs out of onboarding.
            onBackClick =
                when {
                    currentStep > 0 -> bloc::onBackClicked
                    bloc.isDismissible -> bloc::onDismissClicked
                    else -> null
                },
            onSkipClick = bloc::onSkipClicked,
        )
        Children(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            stack = bloc.routerState,
            animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
        ) { child ->
            // Each step needs its own opaque surface: when the flow is re-entered on top of the
            // app, the slide/predictive-back animation draws two steps at once, and without a
            // background the outgoing/incoming pages would show the app (or each other) through.
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = ChefMateTheme.colorScheme.background,
            ) {
                child.instance.bloc.Content()
            }
        }
    }
}
