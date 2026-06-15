package com.plusmobileapps.chefmate.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.GroceryListBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.GroceryListScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = GroceryListBloc.Factory::class,
)
class GroceryListBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<GroceryListBloc.Output>,
) : GroceryListBloc, BlocContext by context {

    override fun onNextClicked() {
        output.onNext(GroceryListBloc.Output.Next)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        GroceryListScreen(bloc = this, modifier = modifier)
    }
}
