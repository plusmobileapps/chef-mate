package com.plusmobileapps.chefmate.onboarding.demo

import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.SingleIn

/**
 * Metro graph for the onboarding demo app. The onboarding flow is fully self-contained — its only
 * non-assisted dependency is `OnboardingRepository`, which auto-injects a `Settings` from
 * `client/shared`'s `@ContributesTo(AppScope)` `SettingsComponent`. So no `@Provides` are needed;
 * the graph just exposes the real [OnboardingRootBloc] factory. Coroutine dispatchers likewise come
 * for free from `shared`'s `CoroutinesComponent`.
 */
@DependencyGraph(AppScope::class)
@SingleIn(AppScope::class)
interface OnboardingDemoComponent {
    val onboardingRootBlocFactory: OnboardingRootBloc.Factory

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(): OnboardingDemoComponent
    }
}
