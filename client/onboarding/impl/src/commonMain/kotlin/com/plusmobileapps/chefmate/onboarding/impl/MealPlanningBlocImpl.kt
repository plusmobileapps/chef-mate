package com.plusmobileapps.chefmate.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.MealPlanningBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.MealPlanningScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = MealPlanningBloc.Factory::class,
)
class MealPlanningBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<MealPlanningBloc.Output>,
) : MealPlanningBloc, BlocContext by context {

    override fun onNextClicked() {
        output.onNext(MealPlanningBloc.Output.Next)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        MealPlanningScreen(bloc = this, modifier = modifier)
    }
}
