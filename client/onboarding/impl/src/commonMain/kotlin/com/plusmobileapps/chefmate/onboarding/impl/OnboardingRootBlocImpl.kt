@file:OptIn(DelicateDecomposeApi::class)

package com.plusmobileapps.chefmate.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.di.OnboardingRepository
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc
import com.plusmobileapps.chefmate.onboarding.OnboardingRootBloc.Output
import com.plusmobileapps.chefmate.onboarding.StartCookingBloc
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.OnboardingRootScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.serialization.Serializable

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = OnboardingRootBloc.Factory::class,
)
class OnboardingRootBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<Output>,
    private val onboardingRepository: OnboardingRepository,
    private val welcome: WelcomeBloc.Factory,
    private val startCooking: StartCookingBloc.Factory,
) : OnboardingRootBloc, BlocContext by context {

    private val navigation = StackNavigation<Configuration>()

    private val stack =
        childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialStack = { listOf(Configuration.Welcome) },
            handleBackButton = true,
            key = "OnboardingRootRouter",
            childFactory = ::createChild,
        )

    override val routerState: Value<ChildStack<*, OnboardingRootBloc.Child>> = stack

    override fun onBackClicked() {
        navigation.pop()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        OnboardingRootScreen(bloc = this, modifier = modifier)
    }

    private fun createChild(config: Configuration, context: BlocContext): OnboardingRootBloc.Child =
        when (config) {
            Configuration.Welcome ->
                OnboardingRootBloc.Child.Welcome(
                    bloc = welcome.create(context = context, output = ::handleWelcomeOutput)
                )

            Configuration.StartCooking ->
                OnboardingRootBloc.Child.StartCooking(
                    bloc =
                        startCooking.create(context = context, output = ::handleStartCookingOutput)
                )
        }

    private fun handleWelcomeOutput(output: WelcomeBloc.Output) {
        when (output) {
            WelcomeBloc.Output.GetStarted -> navigation.bringToFront(Configuration.StartCooking)
        }
    }

    private fun handleStartCookingOutput(output: StartCookingBloc.Output) {
        when (output) {
            StartCookingBloc.Output.StartCooking -> {
                // Persist completion so the flow is never shown again, then hand control back to
                // the
                // root to load the rest of the app.
                onboardingRepository.setOnboardingCompleted()
                this.output.onNext(Output.Finished)
            }
        }
    }

    @Serializable
    private sealed class Configuration {
        @Serializable data object Welcome : Configuration()

        @Serializable data object StartCooking : Configuration()
    }
}
