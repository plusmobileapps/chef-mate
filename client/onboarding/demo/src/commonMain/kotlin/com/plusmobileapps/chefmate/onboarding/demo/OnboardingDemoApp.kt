package com.plusmobileapps.chefmate.onboarding.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.plusmobileapps.chefmate.DefaultBlocContext
import com.plusmobileapps.chefmate.ui.theme.ChefMateTheme

/**
 * Builds the [OnboardingDemoBloc] for the given [componentContext], hosting the real
 * [com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc] behind a debug landing screen.
 *
 * The [component] (the demo Metro graph) is passed in rather than created here, so it can be
 * created once at app scope — the Android `Application` / the desktop `main()` — instead of being
 * recreated every time the host is rebuilt (e.g. on an Android configuration change).
 */
fun buildOnboardingDemoBloc(
    componentContext: ComponentContext,
    component: OnboardingDemoComponent,
): OnboardingDemoBloc =
    OnboardingDemoBlocImpl(
        context = DefaultBlocContext(componentContext = componentContext),
        onboardingRootFactory = component.onboardingRootBlocFactory,
    )

/** Renders the onboarding demo (landing screen + onboarding flow), wrapped in the app theme. */
@Composable
fun OnboardingDemoApp(bloc: OnboardingDemoBloc, modifier: Modifier = Modifier) {
    ChefMateTheme { bloc.Content(modifier) }
}
