package com.plusmobileapps.chefmate.onboarding.impl.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun OnboardingRootScreen(bloc: OnboardingRootBloc, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = bloc.routerState,
        animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
    ) { child ->
        child.instance.bloc.Content()
    }
}
