package com.plusmobileapps.chefmate.onboarding.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.CookModeBloc
import com.plusmobileapps.chefmate.onboarding.impl.ui.CookModeScreen
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(scope = AppScope::class, assistedFactory = CookModeBloc.Factory::class)
class CookModeBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<CookModeBloc.Output>,
) : CookModeBloc, BlocContext by context {

    override fun onNextClicked() {
        output.onNext(CookModeBloc.Output.Next)
    }

    @Composable
    override fun Content(modifier: Modifier) {
        CookModeScreen(bloc = this, modifier = modifier)
    }
}
