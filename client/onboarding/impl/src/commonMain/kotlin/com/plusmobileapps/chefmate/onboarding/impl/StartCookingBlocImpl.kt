package com.plusmobileapps.chefmate.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.StartCookingBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.StartCookingScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = StartCookingBloc.Factory::class,
)
class StartCookingBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<StartCookingBloc.Output>,
) : StartCookingBloc, BlocContext by context {

    override fun onStartCookingClicked() {
        output.onNext(StartCookingBloc.Output.StartCooking)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        StartCookingScreen(bloc = this, modifier = modifier)
    }
}
