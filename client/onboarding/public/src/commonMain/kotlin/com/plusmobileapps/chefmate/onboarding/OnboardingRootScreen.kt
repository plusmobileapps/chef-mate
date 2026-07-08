package com.plusmobileapps.chefmate.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
            // Nothing to go back to on the first step; the arrow is hidden there.
            onBackClick = if (currentStep == 0) null else bloc::onBackClicked,
            onSkipClick = bloc::onSkipClicked,
        )
        Children(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            stack = bloc.routerState,
            animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
        ) { child ->
            child.instance.bloc.Content()
        }
    }
}
