package com.plusmobileapps.chefmate.onboarding.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.DefaultBlocContext
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import dev.zacsweers.metro.createGraphFactory

/**
 * Builds the [OnboardingDemoBloc] for the given [componentContext]. It hosts the real
 * [com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc] (resolved from the demo Metro graph)
 * behind a debug landing screen.
 */
fun buildOnboardingDemoBloc(componentContext: ComponentContext): OnboardingDemoBloc {
    val component = createGraphFactory<OnboardingDemoComponent.Factory>().create()
    return OnboardingDemoBlocImpl(
        context = DefaultBlocContext(componentContext = componentContext),
        onboardingRootFactory = component.onboardingRootBlocFactory,
    )
}

/** Renders the onboarding demo (landing screen + onboarding flow), wrapped in the app theme. */
@Composable
fun OnboardingDemoApp(bloc: OnboardingDemoBloc, modifier: Modifier = Modifier) {
    ChefMateTheme { bloc.Content(modifier) }
}
