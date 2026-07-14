package com.plusmobileapps.chefmate.onboarding.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.plusmobileapps.chefmate.ui.Content
import com.plusmobileapps.chefmate.ui.backAnimation

@Composable
fun OnboardingDemoScreen(bloc: OnboardingDemoBloc, modifier: Modifier = Modifier) {
    Children(
        modifier = modifier.fillMaxSize(),
        stack = bloc.routerState,
        animation = backAnimation(backHandler = bloc.backHandler, onBack = bloc::onBackClicked),
    ) { child ->
        child.instance.bloc.Content()
    }
}

@Composable
fun LandingScreen(bloc: LandingBloc, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Onboarding Demo", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Debug launcher for the onboarding flow.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Button(onClick = bloc::onStartOnboardingClicked) { Text("Start onboarding") }
    }
}
