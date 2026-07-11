package com.plusmobileapps.chefmate.onboarding.impl

import com.plusmobileapps.chefmate.BlocContext
import com.plusmobileapps.chefmate.Consumer
import com.plusmobileapps.chefmate.di.AppScope
import com.plusmobileapps.chefmate.onboarding.AddToGroceryBloc
import com.plusmobileapps.metro.extensions.assistedfactory.ContributesAssistedFactory
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@ContributesAssistedFactory(
    scope = AppScope::class,
    assistedFactory = AddToGroceryBloc.Factory::class,
)
class AddToGroceryBlocImpl(
    @Assisted context: BlocContext,
    @Assisted private val output: Consumer<AddToGroceryBloc.Output>,
) : AddToGroceryBloc, BlocContext by context {

    override fun onNextClicked() {
        output.onNext(AddToGroceryBloc.Output.Next)
    }
}
