package com.plusmobileapps.chefmate.onboarding.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.WelcomeBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = WelcomeBloc.Factory::class)
class WelcomeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<WelcomeBloc.Output>,
) : WelcomeBloc, BlocContext by context {

    override fun onGetStartedClicked() {
        output.onNext(WelcomeBloc.Output.GetStarted)
    }

    override fun onSignInClicked() {
        output.onNext(WelcomeBloc.Output.SignIn)
    }

    override fun onSkipClicked() {
        output.onNext(WelcomeBloc.Output.Skip)
    }
}
