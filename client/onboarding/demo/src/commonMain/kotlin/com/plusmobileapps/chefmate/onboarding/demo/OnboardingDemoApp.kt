package com.plusmobileapps.chefmate.onboarding.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.DefaultBlocContext
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme
import dev.zacsweers.metro.createGraphFactory

/**
 * Builds the real [OnboardingRootBloc] from the demo graph for the given [componentContext]. The
 * root's outputs (Finished / SignIn) are logged rather than handled — the demo showcases the
 * onboarding flow itself, not what the app does afterwards.
 */
fun buildOnboardingDemoBloc(componentContext: ComponentContext): OnboardingRootBloc {
    val component = createGraphFactory<OnboardingDemoComponent.Factory>().create()
    return component.onboardingRootBlocFactory.create(
        context = DefaultBlocContext(componentContext = componentContext),
        output = Consumer { output -> println("OnboardingDemo bloc output: $output") },
    )
}

/** Renders the onboarding flow for the demo app, wrapped in the app theme. */
@Composable
fun OnboardingDemoApp(bloc: OnboardingRootBloc, modifier: Modifier = Modifier) {
    ChefMateTheme { bloc.Content(modifier) }
}
