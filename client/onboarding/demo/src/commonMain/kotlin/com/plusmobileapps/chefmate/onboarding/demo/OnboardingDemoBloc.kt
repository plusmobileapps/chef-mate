package com.plusmobileapps.chefmate.onboarding.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackHandlerOwner
import com.plusmobileapps.chefmate.BackClickBloc
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import com.plusmobileapps.chefmate.ui.ComposeScreen

/**
 * Root navigation BLoC for the onboarding demo app. It hosts a [OnboardingRootBloc] behind a simple
 * debug landing screen: the landing screen opens the onboarding flow, and when the flow reports it
 * has finished (or the user picks sign-in), the demo navigates back to the landing screen so it can
 * be launched again. This exists only in the demo module — production has no equivalent.
 */
interface OnboardingDemoBloc : ComposeScreen, BackHandlerOwner, BackClickBloc {
    val routerState: Value<ChildStack<*, Child>>

    @Composable
    override fun Content(modifier: Modifier) {
        OnboardingDemoScreen(bloc = this, modifier = modifier)
    }

    sealed class Child {
        abstract val bloc: ComposeScreen

        data class Landing(override val bloc: LandingBloc) : Child()

        data class Onboarding(override val bloc: OnboardingRootBloc) : Child()
    }
}

/** The debug landing screen shown before the onboarding flow is launched. */
interface LandingBloc : ComposeScreen {
    fun onStartOnboardingClicked()

    @Composable
    override fun Content(modifier: Modifier) {
        LandingScreen(bloc = this, modifier = modifier)
    }
}
