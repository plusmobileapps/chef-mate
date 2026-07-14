@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.onboarding.demo

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import kotlinx.serialization.Serializable

class OnboardingDemoBlocImpl(
    context: BlocContext,
    private val onboardingRootFactory: OnboardingRootBloc.Factory,
) : OnboardingDemoBloc, BlocContext by context {

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.Landing) },
            handleBackButton = true,
            key = "OnboardingDemoRouter",
            childFactory = ::createChild,
        )

    override val routerState: Value<ChildStack<*, OnboardingDemoBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    private fun createChild(config: Configuration, context: BlocContext): OnboardingDemoBloc.Child =
        when (config) {
            Configuration.Landing ->
                OnboardingDemoBloc.Child.Landing(
                    bloc =
                        LandingBlocImpl(
                            onStartOnboarding = {
                                navigation.bringToFront(Configuration.Onboarding)
                            }
                        )
                )

            Configuration.Onboarding ->
                OnboardingDemoBloc.Child.Onboarding(
                    bloc =
                        onboardingRootFactory.create(
                            context = context,
                            props = OnboardingRootBloc.Props(),
                            output = ::handleOnboardingOutput,
                        )
                )
        }

    private fun handleOnboardingOutput(output: OnboardingRootBloc.Output) {
        when (output) {
            // Every terminal outcome returns to the debug landing screen so the flow can be
            // relaunched; the demo has no real app or auth flow to hand off to.
            OnboardingRootBloc.Output.Finished,
            OnboardingRootBloc.Output.Dismissed,
            OnboardingRootBloc.Output.SignIn,
            OnboardingRootBloc.Output.SignUp -> navigation.bringToFront(Configuration.Landing)
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable data object Landing : Configuration()

        @Serializable data object Onboarding : Configuration()
    }
}

internal class LandingBlocImpl(private val onStartOnboarding: () -> Unit) : LandingBloc {
    override fun onStartOnboardingClicked() {
        onStartOnboarding()
    }
}
